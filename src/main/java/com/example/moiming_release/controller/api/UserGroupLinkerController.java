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

        // TODO: 줄였으면 해당 MoimingGroup 의 cntMember-1 을 해줘야 함!
        if (findData.isPresent()) {

            UserGroupLinker targetData = findData.get();

            linkerRepository.delete(targetData);

            MoimingGroup thisGroup = groupRepository.findById(UUID.fromString(groupUuid)).get();

            if (thisGroup.getGroupMemberCnt() == 1) { // 마지막 멤버면 삭제하면됨

                groupRepository.delete(thisGroup);

            } else { // 아직 남았으면 member update

                int groupMemberCnt = thisGroup.getGroupMemberCnt();
                groupMemberCnt--;

                thisGroup.setGroupMemberCnt(groupMemberCnt);
                thisGroup.setUpdatedAt(LocalDateTime.now().withNano(0));
                groupRepository.save(thisGroup);

            }
            return TransferModel.OK();

        } else {

            return TransferModel.ERROR(500, "해당 연결은 존재하지 않습니다");

        }
    }

    @PostMapping("/link")
    public TransferModel<List<String>> joinGroupRequest(@RequestBody TransferModel<List<UserGroupUuidDTO>> receiveModel) {

        List<UserGroupUuidDTO> receivedList = receiveModel.getData();
        String groupUuid = receivedList.get(0).getGroupUuid(); // 어차피 다 똑같.

        List<String> addedMemberUuidResponse = new ArrayList<>();
        Optional<MoimingGroup> groupOptional = groupRepository.findById(UUID.fromString(groupUuid));

        if (groupOptional.isPresent()) {

            MoimingGroup thisGroup = groupOptional.get(); // 그룹 존재 여부 확인
            String requestUserUuid = null;

            int addedMember = 0;

            for (int i = 0; i < receivedList.size(); i++) { // 받아온 추가 리스트 돌리기 시작

                boolean isMember = false;

                UserGroupUuidDTO chunkData = receivedList.get(i); // 한 명씩 불러옴
                String userUuid = chunkData.getUserUuid(); // 추가되는 인원

                requestUserUuid = chunkData.getRequestUserUuid(); // 초대한 인원 (Noti 추가용)


                Optional<UserGroupLinker> ugLinker = linkerRepository.findByMoimingUserUuidAndMoimingGroupUuid(UUID.fromString(userUuid), UUID.fromString(groupUuid));
                if (ugLinker.isPresent()) isMember = true; // 추가되는 인원가 해당 그룹이 이미 연결 되어 있으면 하기 실행 x

                if (!isMember) { // 우리 멤버가 아니다.

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

                    String textNoti = reqUserName + "님이 회원님을 " + thisGroup.getGroupName() + "에 초대하셨습니다. 같이 즐거운 모임 만들어 나가세요!";

                    // 새로 초대된 유저에게 저장되는 알림
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

            if (addedMemberUuidResponse.size() != 0) { // 추가되기 전 그룹을 보내야 새 멤버는 알림에서 제외한다
                notificationToGroupMembers(thisGroup, requestUserUuid, addedMemberUuidResponse);
            }


            return TransferModel.OK(addedMemberUuidResponse);

        } else {
            // 유효하지 않는 그룹입니다.
            return TransferModel.ERROR(200, "Group Does Not Exist");

        }
    }

    private void notificationToGroupMembers(MoimingGroup preGroup, String requestUserUuid, List<String> addedMemberUuid) {

        List<UserGroupLinker> ugLinkers = preGroup.getGroupUserList(); // 기존 그룹원들
        int addedMemberCnt = addedMemberUuid.size(); // 1인일경우, n인일 경우 가는 알림이 다름

        Optional<MoimingUser> findSingleUser = userRepository.findById(UUID.fromString(addedMemberUuid.get(0)));
        String userName = findSingleUser.get().getUserName();
        String textNoti;

        if (addedMemberCnt > 1) {
            textNoti = userName + "님 외 " + (addedMemberCnt - 1) + "명이 " + preGroup.getGroupName() + "에 초대되었습니다! 환영해주세요!";
        } else { // 1 일경우
            textNoti = userName + "님이 " + preGroup.getGroupName() + "에 초대되었습니다! 환영해주세요!";
        }


        for (int i = 0; i < ugLinkers.size(); i++) {

            UserGroupLinker ugLinker = ugLinkers.get(i);

            // 이제 초대받은 사람들은 여기에 들어가면 안됨 ..
            if (!addedMemberUuid.contains(ugLinker.getMoimingUser().getUuid().toString())) { // TODO: 이거 N^2 함수인듯

                if (!ugLinker.getMoimingUser().getUuid().toString().equals(requestUserUuid)) {//TODO: 초대한 사람에게는 보낼필요 없음

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
