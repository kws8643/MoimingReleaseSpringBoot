package com.example.moiming_release.controller.api;

import com.example.moiming_release.model.entity.MoimingGroup;
import com.example.moiming_release.model.entity.MoimingUser;
import com.example.moiming_release.model.entity.Notification;
import com.example.moiming_release.model.entity.UserGroupLinker;
import com.example.moiming_release.model.network.TransferModel;
import com.example.moiming_release.model.network.request.UserGroupLinkerRequestDTO;
import com.example.moiming_release.model.network.response.UserGroupLinkerResponseDTO;
import com.example.moiming_release.model.other.UserGroupUuidDTO;
import com.example.moiming_release.repository.MoimingGroupRepository;
import com.example.moiming_release.repository.MoimingUserRepository;
import com.example.moiming_release.repository.NotificationRepository;
import com.example.moiming_release.repository.UserGroupLinkerRepository;
import com.example.moiming_release.service.UserGroupLinkerLogicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/userGroupLinker")
public class UserGroupLinkerController {

    @Autowired
    private MoimingUserRepository userRepository;

    @Autowired
    private MoimingGroupRepository groupRepository;

    @Autowired
    private UserGroupLinkerRepository linkerRepository;

    @Autowired
    private UserGroupLinkerLogicService linkerLogicService;

    @Autowired
    private NotificationRepository notiRepository;

    @GetMapping("/{uuid}")
    public TransferModel<List<UserGroupLinker>> read(@PathVariable String uuid) {

        return linkerLogicService.read(uuid);

    }

    public TransferModel<UserGroupLinkerResponseDTO> update(@RequestBody TransferModel<UserGroupLinkerRequestDTO> request) {
        return null;
    }


    @PostMapping("/unlink")
    public TransferModel delete(@RequestBody TransferModel<UserGroupUuidDTO> receiveModel) {

        UserGroupUuidDTO chunkData = receiveModel.getData();
        String userUuid = chunkData.getUserUuid();
        String groupUuid = chunkData.getGroupUuid();

        Optional<UserGroupLinker> findData = linkerRepository.findByMoimingUserUuidAndMoimingGroupUuid(UUID.fromString(userUuid), UUID.fromString(groupUuid));

        // TODO: ???????????? ?????? MoimingGroup ??? cntMember-1 ??? ????????? ???!
        if (findData.isPresent()) {

            UserGroupLinker targetData = findData.get();

            linkerRepository.delete(targetData);

            MoimingGroup thisGroup = groupRepository.findById(UUID.fromString(groupUuid)).get();

            if (thisGroup.getGroupMemberCnt() == 1) { // ????????? ????????? ???????????????

                groupRepository.delete(thisGroup);

            } else { // ?????? ???????????? member update

                int groupMemberCnt = thisGroup.getGroupMemberCnt();
                groupMemberCnt--;

                thisGroup.setGroupMemberCnt(groupMemberCnt);
                thisGroup.setUpdatedAt(LocalDateTime.now().withNano(0));
                groupRepository.save(thisGroup);

            }
            return TransferModel.OK();

        } else {

            return TransferModel.ERROR(500, "?????? ????????? ???????????? ????????????");

        }
    }

    @PostMapping("/link")
    public TransferModel<List<String>> joinGroupRequest(@RequestBody TransferModel<List<UserGroupUuidDTO>> receiveModel) {

        List<UserGroupUuidDTO> receivedList = receiveModel.getData();
        String groupUuid = receivedList.get(0).getGroupUuid(); // ????????? ??? ??????.

        List<String> addedMemberUuidResponse = new ArrayList<>();
        Optional<MoimingGroup> groupOptional = groupRepository.findById(UUID.fromString(groupUuid));

        if (groupOptional.isPresent()) {

            MoimingGroup thisGroup = groupOptional.get(); // ?????? ?????? ?????? ??????
            String requestUserUuid = null;

            int addedMember = 0;

            for (int i = 0; i < receivedList.size(); i++) { // ????????? ?????? ????????? ????????? ??????

                boolean isMember = false;

                UserGroupUuidDTO chunkData = receivedList.get(i); // ??? ?????? ?????????
                String userUuid = chunkData.getUserUuid(); // ???????????? ??????

                requestUserUuid = chunkData.getRequestUserUuid(); // ????????? ?????? (Noti ?????????)


                Optional<UserGroupLinker> ugLinker = linkerRepository.findByMoimingUserUuidAndMoimingGroupUuid(UUID.fromString(userUuid), UUID.fromString(groupUuid));
                if (ugLinker.isPresent()) isMember = true; // ???????????? ????????? ?????? ????????? ?????? ?????? ?????? ????????? ?????? ?????? x

                if (!isMember) { // ?????? ????????? ?????????.

                    Optional<MoimingUser> newMember = userRepository.findById(UUID.fromString(userUuid));

                    UserGroupLinker newLinker = UserGroupLinker.builder()
                            .moimingUser(newMember.get())
                            .moimingGroup(thisGroup)
                            .createdAt(LocalDateTime.now().withNano(0))
                            .build();

                    linkerRepository.save(newLinker);
                    addedMember++;
                    addedMemberUuidResponse.add(userUuid);

                    Optional<MoimingUser> requestingUser = userRepository.findById(UUID.fromString(requestUserUuid));
                    String reqUserName = requestingUser.get().getUserName();

                    String textNoti = reqUserName + "?????? ???????????? " + thisGroup.getGroupName() + "??? ?????????????????????. ?????? ????????? ?????? ????????? ????????????!";

                    // ?????? ????????? ???????????? ???????????? ??????
                    Notification invitedNoti = Notification.builder()
                            .isRead(false)
                            .sentActivity("group")
                            .sentGroupUuid(thisGroup.getUuid())
                            .sentUserUuid(UUID.fromString(requestUserUuid))
                            .msgType(1)
                            .msgText(textNoti)
                            .moimingUser(newMember.get())
                            .createdAt(LocalDateTime.now().withNano(0))
                            .build();

                    notiRepository.save(invitedNoti);

                }
            }

            int memberCnt = thisGroup.getGroupMemberCnt();
            int updatedMemberCnt = memberCnt + addedMember;
            thisGroup.setGroupMemberCnt(updatedMemberCnt);
            thisGroup.setUpdatedAt(LocalDateTime.now().withNano(0));

            groupRepository.save(thisGroup);

            if (addedMemberUuidResponse.size() != 0) { // ???????????? ??? ????????? ????????? ??? ????????? ???????????? ????????????
                notificationToGroupMembers(thisGroup, requestUserUuid, addedMemberUuidResponse);
            }


            return TransferModel.OK(addedMemberUuidResponse);

        } else {
            // ???????????? ?????? ???????????????.
            return TransferModel.ERROR(200, "Group Does Not Exist");

        }
    }

    private void notificationToGroupMembers(MoimingGroup preGroup, String requestUserUuid, List<String> addedMemberUuid) {

        List<UserGroupLinker> ugLinkers = preGroup.getGroupUserList(); // ?????? ????????????
        int addedMemberCnt = addedMemberUuid.size(); // 1????????????, n?????? ?????? ?????? ????????? ??????

        Optional<MoimingUser> findSingleUser = userRepository.findById(UUID.fromString(addedMemberUuid.get(0)));
        String userName = findSingleUser.get().getUserName();
        String textNoti;

        if (addedMemberCnt > 1) {
            textNoti = userName + "??? ??? " + (addedMemberCnt - 1) + "?????? " + preGroup.getGroupName() + "??? ?????????????????????! ??????????????????!";
        } else { // 1 ?????????
            textNoti = userName + "?????? " + preGroup.getGroupName() + "??? ?????????????????????! ??????????????????!";
        }


        for (int i = 0; i < ugLinkers.size(); i++) {

            UserGroupLinker ugLinker = ugLinkers.get(i);

            // ?????? ???????????? ???????????? ????????? ???????????? ?????? ..
            if (!addedMemberUuid.contains(ugLinker.getMoimingUser().getUuid().toString())) { // TODO: ?????? N^2 ????????????

                if (!ugLinker.getMoimingUser().getUuid().toString().equals(requestUserUuid)) {//TODO: ????????? ??????????????? ???????????? ??????

                    Notification preMemberNoti = Notification.builder()
                            .isRead(false)
                            .sentActivity("group")
                            .sentGroupUuid(preGroup.getUuid())
                            .sentUserUuid(UUID.fromString(requestUserUuid))
                            .msgType(0)
                            .msgText(textNoti)
                            .moimingUser(ugLinker.getMoimingUser())
                            .createdAt(LocalDateTime.now().withNano(0))
                            .build();

                    notiRepository.save(preMemberNoti);

                }
            }
        }

    }

}
