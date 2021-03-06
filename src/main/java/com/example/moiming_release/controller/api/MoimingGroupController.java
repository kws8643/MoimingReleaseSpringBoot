package com.example.moiming_release.controller.api;

import com.example.moiming_release.model.entity.*;
import com.example.moiming_release.model.network.TransferModel;
import com.example.moiming_release.model.network.request.MoimingGroupRequestDTO;
import com.example.moiming_release.model.network.request.MoimingSessionRequestDTO;
import com.example.moiming_release.model.network.request.UserSessionLinkerRequestDTO;
import com.example.moiming_release.model.network.response.MoimingGroupResponseDTO;
import com.example.moiming_release.model.other.*;
import com.example.moiming_release.repository.*;
import com.example.moiming_release.service.MoimingGroupLogicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/group")
public class MoimingGroupController {

    @Autowired
    private MoimingGroupLogicService moimingGroupLogicService;

    @Autowired
    private MoimingGroupRepository groupRepository;

    @Autowired
    private MoimingSessionRepository sessionRepository;

    @Autowired
    private MoimingUserRepository userRepository;

    @Autowired
    private UserGroupLinkerRepository ugLinkerRepository;

    @Autowired
    private UserSessionLinkerRepository usLinkerRepository;

    @Autowired
    private NonMoimingUserRepository nmuRepository;

    @Autowired
    private NotificationRepository notiRepository;

    @PostMapping("/create")
    public TransferModel<MoimingGroupResponseDTO> create(@RequestBody TransferModel<MoimingGroupRequestDTO> request) {
        return moimingGroupLogicService.create(request);
    }

    @PostMapping("/update")
    public TransferModel<MoimingGroupResponseDTO> update(@RequestBody TransferModel<MoimingGroupEditInfoDto> request) {

        return moimingGroupLogicService.update(request);
    }



    public TransferModel<MoimingGroupResponseDTO> read(String uuid) {
        return null;
    }




    public TransferModel delete(String uuid) {
        return null;
    }


    // MoimingMembersDTO??? ????????? ??????.
    @GetMapping("/getGroupMembers/{groupUuid}")
    public TransferModel<List<MoimingMembersDTO>> giveGroupMembers(@PathVariable String groupUuid) {

        Optional<MoimingGroup> optional = groupRepository.findById(UUID.fromString(groupUuid));

        List<MoimingMembersDTO> responseData = new ArrayList<>();

        if (optional.isPresent()) {

            MoimingGroup thisGroup = optional.get();
            List<UserGroupLinker> linkerList = thisGroup.getGroupUserList();

            // ????????? ?????? User ???????????? ??????
            for (UserGroupLinker linker : linkerList) {

                MoimingUser member = linker.getMoimingUser();
                MoimingMembersDTO sendMember = MoimingMembersDTO.builder()
                        .uuid(member.getUuid())
                        .oauthUid(member.getOauthUid())
                        .userName(member.getUserName())
                        .userPfImg(member.getUserPfImg())
                        .bankName(member.getBankName())
                        .bankNumber(member.getBankNumber())
                        .build();

                System.out.println(sendMember.toString());

                responseData.add(sendMember);
            }
            // ????????? ????????? ?????? GroupMemberDTO ??????
            // list ??? ?????? ??????.

            return TransferModel.OK(responseData);


        } else {

            // ????????? ?????? ??????.
            return TransferModel.ERROR(404, "???????????? ?????? ???????????????. ???????????? ???????????????.");
        }

    }

    @PostMapping("/getGroupSessions/{groupUuid}/{notificationCheck}")
    public TransferModel<List<SessionAndUserStatusDTO>> requestGroupSessions(@PathVariable String groupUuid, @RequestBody String userUuid
            , @PathVariable Integer notificationCheck) { // curUserUuid ??? ?????????.

        String thisUserUuid = userUuid.replace("\"", "");
        System.out.println("1: " + groupUuid + "\n2: " + thisUserUuid);

        boolean isNotiCheckNeeded = false;
        if (notificationCheck == 1) isNotiCheckNeeded = true;

        List<SessionAndUserStatusDTO> sessionAndStatusList = new ArrayList<>();
        List<MoimingSession> sessionList = sessionRepository.findByMoimingGroupUuid(UUID.fromString(groupUuid)).get();

        // ??? ????????? ????????? ?????? ?????? ?????????
        for (MoimingSession session : sessionList) {

            SessionAndUserStatusDTO singleDTO = new SessionAndUserStatusDTO(); // ????????? ????????? ???

            singleDTO.setMoimingSession(session);

            // 1. ?????? ????????????? 0 = ??? ??????
            if (session.getSessionCreatorUuid().toString().equals(thisUserUuid)) {

                singleDTO.setCurUserStatus(0);

            } else { // ?????? ????????? ?????? ??????

                // ?????? ????????? ?????? ????????? ????????? ????????????.
                MoimingUser creator = userRepository.findById(session.getSessionCreatorUuid()).get();
                singleDTO.setCreatorName(creator.getUserName());

                // ???????????? ????????? ???????????? ?????? ???????????? ????????????.
                Optional<UserSessionLinker> requestLinker = usLinkerRepository.findByMoimingSessionUuidAndMoimingUserUuid(session.getUuid(), UUID.fromString(thisUserUuid));

                if (requestLinker.isPresent()) {

                    UserSessionLinker curUserLinker = requestLinker.get();

                    singleDTO.setCurUserCost(curUserLinker.getPersonalCost()); // ??? ????????? ?????? ?????? ??????

                    if (curUserLinker.isSent()) {
                        singleDTO.setCurUserStatus(2); // ?????? ??????????????? Set

                    } else {
                        // Notification (?????? ?????? ?????? -> ??? ????????????, Type 2 ??? ????????? ???????????? ?????????)
                        // TODO: ????????? ????????? Notification ?????? ????????? isRead ???????????? ???????

                        Optional<Notification> preNoti = notiRepository.findBySentUserUuidAndSentSessionUuidAndMsgType(UUID.fromString(thisUserUuid), session.getUuid(), 2);

                        if (preNoti.isPresent()) { // ?????? ?????? ????????? ?????? ?????????
                            singleDTO.setCurUserStatus(3);
                        } else { // ?????? ?????? ????????? ????????? ?????? ????????? TODO: ????????????
                            singleDTO.setCurUserStatus(1);
                        }
                    }
                } else { // ??????????????? ????????????.

                    singleDTO.setCurUserStatus(4);
                }

            }
            sessionAndStatusList.add(singleDTO);
        }

        boolean isNotiChanged = false;
        // ???????????? ????????? ?????? ????????? ?????? ???????????? ?????? ?????????????????????.
        if (isNotiCheckNeeded) {

            isNotiChanged = checkUserGroupNotification(thisUserUuid, groupUuid);
        }

        if (isNotiChanged) return TransferModel.OK(sessionAndStatusList, "changed");
        else return TransferModel.OK(sessionAndStatusList);

    }

    private boolean checkUserGroupNotification(String userUuid, String groupUuid) {

        Optional<List<Notification>> findNotiList = notiRepository.findByMoimingUserUuidAndSentGroupUuid(UUID.fromString(userUuid)
                , UUID.fromString(groupUuid));

        List<Notification> notiList = findNotiList.get();

        boolean isNotiChanged = false;

        for (int i = 0; i < notiList.size(); i++) {

            Notification thisNoti = notiList.get(i);

            if (thisNoti.getSentActivity().equals("group") && !(thisNoti.getIsRead())) {

                thisNoti.setIsRead(true);

                notiRepository.save(thisNoti);

                isNotiChanged = true;
            }
        }

        return isNotiChanged;
    }


    @PostMapping("/getGroupAndMembers")
    public TransferModel<MoimingGroupAndMembersDTO> getRefreshedGroupInfo(@RequestBody String groupUuid) {

        String thisGroupUuid = groupUuid.replace("\"", "");

        Optional<MoimingGroup> isGroupPresent = groupRepository.findById(UUID.fromString(thisGroupUuid));

        if (isGroupPresent.isPresent()) {

            MoimingGroup thisGroup = isGroupPresent.get(); // ?????? ????????? ??????.

            MoimingGroupAndMembersDTO transferData = MoimingGroupAndMembersDTO.builder()
                    .moimingGroupDto(thisGroup)
                    .moimingMembersList(searchGroupMembers(thisGroup))
                    .build();

            return TransferModel.OK(transferData);

        } else {

            return TransferModel.ERROR(404, "No Group Found");
        }

    }

    private List<MoimingMembersDTO> searchGroupMembers(MoimingGroup group) {

        List<MoimingMembersDTO> groupMembersDTO = new ArrayList<>();
        List<UserGroupLinker> linkerList = group.getGroupUserList();

        // ????????? ?????? User ???????????? ??????
        for (UserGroupLinker linker : linkerList) {

            MoimingUser member = linker.getMoimingUser();
            MoimingMembersDTO memberDTO = MoimingMembersDTO.builder()
                    .uuid(member.getUuid())
                    .oauthUid(member.getOauthUid())
                    .userName(member.getUserName())
                    .userPfImg(member.getUserPfImg())
                    .bankName(member.getBankName())
                    .bankNumber(member.getBankNumber())
                    .build();

            groupMembersDTO.add(memberDTO);
        }

        return groupMembersDTO;
    }

    @PostMapping("/createWithSession")
    public TransferModel<MoimingGroupAndMembersDTO> createWithSessions(@RequestBody TransferModel<GroupAndSessionCreationDTO> requestModel) {

        GroupAndSessionCreationDTO requestDto = requestModel.getData();

        //TODO: 1. ?????? ??????
        MoimingGroupRequestDTO groupRequest = requestDto.getGroupRequest();

        MoimingGroup creatingGroup = MoimingGroup.builder()
                .groupName(groupRequest.getGroupName())
                .groupInfo(groupRequest.getGroupInfo())
                .groupCreatorUuid(groupRequest.getGroupCreatorUuid())
                .bgImg(groupRequest.getBgImg())
                .groupMemberCnt(groupRequest.getGroupMemberCnt())
                .createdAt(LocalDateTime.now().withNano(0))
                .groupPayment(0)
                .build();

        MoimingGroup savedGroup = groupRepository.save(creatingGroup);

        Optional<MoimingUser> findCreatingUser = userRepository.findById(savedGroup.getGroupCreatorUuid());
        MoimingUser creatingUser = findCreatingUser.get();

        //TODO: 2. ????????? ??????, ????????? ???????????? ????????? List ??? ???????????? ?????? ????????? ???????????? ????????? ???.
        List<UUID> membersUuidList = requestDto.getMembersUuidList();
        List<MoimingMembersDTO> responseMemberList = new ArrayList<>();

        for (int i = 0; i < membersUuidList.size(); i++) {

            Optional<MoimingUser> findUser = userRepository.findById(membersUuidList.get(i));
            MoimingUser groupMember = findUser.get();

            //UserGroupLinking ??? ???????????????.
            UserGroupLinker ugLinker = UserGroupLinker.builder()
                    .moimingGroup(savedGroup)
                    .moimingUser(groupMember)
                    .createdAt(LocalDateTime.now().withNano(0))
//                    .noticeCnt(0)
//                    .recentNotice(null)
                    .build();

            ugLinkerRepository.save(ugLinker);

            responseMemberList.add(MoimingMembersDTO.builder()
                    .uuid(groupMember.getUuid())
                    .oauthUid(groupMember.getOauthUid())
                    .userName(groupMember.getUserName())
                    .bankName(groupMember.getBankName())
                    .bankNumber(groupMember.getBankNumber())
                    .userPfImg(groupMember.getUserPfImg())
                    .build());

            // ????????? ???????????? ????????? Notification ??????!
            String creatingUserName = creatingUser.getUserName();


            String textNoti = creatingUserName + "?????? ???????????? " + savedGroup.getGroupName() + "??? ?????????????????????. ?????? ????????? ?????? ????????? ????????????!";

            // ?????? ????????? ???????????? ???????????? ????????? ???????????? ???.
            if (!groupMember.getUuid().toString().equals(creatingUser.getUuid().toString())) { // ?????? ?????? ?????? ??????
                // ?????? ????????? ???????????? ???????????? ??????
                Notification invitedNoti = Notification.builder()
                        .isRead(false)
                        .sentActivity("group")
                        .sentGroupUuid(savedGroup.getUuid())
                        .sentUserUuid(findCreatingUser.get().getUuid())
                        .msgType(1)
                        .msgText(textNoti)
                        .moimingUser(groupMember)
                        .createdAt(LocalDateTime.now().withNano(0))
                        .build();

                notiRepository.save(invitedNoti);
            }

        }

        //TODO: 3. ?????? ??????
        MoimingSessionRequestDTO sessionRequest = requestDto.getSessionRequest();

        MoimingSession createSesssion = MoimingSession.builder()
                .sessionCreatorUuid(savedGroup.getGroupCreatorUuid())
                .moimingGroup(savedGroup)
                .sessionType(sessionRequest.getSessionType())
                .sessionName(sessionRequest.getSessionName())
                .sessionMemberCnt(sessionRequest.getSessionMemberCnt())
                .curSenderCnt(1)  // ????????? ????????? ???.
                .totalCost(sessionRequest.getTotalCost())
                .singleCost(sessionRequest.getSingleCost())
                .curCost(sessionRequest.getSessionCreatorCost()) // ????????? ?????? ???
                .isFinished(false)
                .createdAt(LocalDateTime.now().withNano(0))
                .build();

        MoimingSession savedSession = sessionRepository.save(createSesssion);


        //TODO: 4. ?????? ?????? ??????
        //TODO: 5. ????????? ?????? ??????
        List<UserSessionLinkerRequestDTO> usDataList = requestDto.getUsDataList();

        for (int i = 0; i < usDataList.size(); i++) {

            UserSessionLinkerRequestDTO requestData = usDataList.get(i);

            if (requestData.getIsMoimingUser()) { // ????????? ??????????????? USLinkLogic ??????

                if (requestData.getMoimingUserUuid().toString().equals(creatingUser.getUuid().toString())) {
                    createUserLinker(requestData, true, savedSession);
                } else {
                    createUserLinker(requestData, false, savedSession);
                }

            } else { // ???????????? NMULinkLogic ??????

                createNmuLinker(requestData, savedSession);

            }
        }


        MoimingGroupAndMembersDTO transferData = MoimingGroupAndMembersDTO.builder()
                .moimingGroupDto(savedGroup)
                .moimingMembersList(responseMemberList)
                .build();

        return TransferModel.OK(transferData);

    }


    public void createUserLinker(UserSessionLinkerRequestDTO requestData, boolean isCreator, MoimingSession savedSession) {

        MoimingUser userFound = userRepository.findById(requestData.getMoimingUserUuid()).get();

        UserSessionLinker insertLinker = UserSessionLinker.builder()
                .personalCost(requestData.getPersonalCost())
                .moimingUser(userFound)
                .moimingSession(savedSession)
                .createdAt(LocalDateTime.now().withNano(0))
                .build();

        if (isCreator) {
            insertLinker.setSent(true);
        } else {
            insertLinker.setSent(false);
        }

        UserSessionLinker linked = usLinkerRepository.save(insertLinker);

    }

    public boolean createNmuLinker(UserSessionLinkerRequestDTO requestData, MoimingSession savedSession) {

        NonMoimingUser nmu = NonMoimingUser.builder()
                .nmuName(requestData.getUserName())
                .isNmuSent(false)
                .moimingSession(savedSession)
                .nmuPersonalCost(requestData.getPersonalCost())
                .createdAt(LocalDateTime.now().withNano(0))
                .build();

        NonMoimingUser nmuCreated = nmuRepository.save(nmu);

        return true;

    }

    @PostMapping("/setNotice")
    public TransferModel<MoimingGroupResponseDTO> groupNoticeRequest(@RequestBody TransferModel<GroupNoticeDTO> requestModel) {


        return moimingGroupLogicService.groupNoticeRequest(requestModel.getData());

    }
}
