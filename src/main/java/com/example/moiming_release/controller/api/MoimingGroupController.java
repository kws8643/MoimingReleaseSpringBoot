package com.example.moiming_release.controller.api;

import com.example.moiming_release.controller.intf.CrudInterface;
import com.example.moiming_release.model.entity.*;
import com.example.moiming_release.model.network.TransferModel;
import com.example.moiming_release.model.network.request.MoimingGroupRequestDTO;
import com.example.moiming_release.model.network.request.MoimingSessionRequestDTO;
import com.example.moiming_release.model.network.request.UserSessionLinkerRequestDTO;
import com.example.moiming_release.model.network.response.MoimingGroupResponseDTO;
import com.example.moiming_release.model.other.*;
import com.example.moiming_release.repository.*;
import com.example.moiming_release.service.MoimingGroupLogicService;
import com.sun.mail.auth.Ntlm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/group")
public class MoimingGroupController implements CrudInterface<MoimingGroupRequestDTO, MoimingGroupResponseDTO> {

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

    @Override
    @PostMapping("/create")
    public TransferModel<MoimingGroupResponseDTO> create(@RequestBody TransferModel<MoimingGroupRequestDTO> request) {
        return moimingGroupLogicService.create(request);
    }


    @Override
    public TransferModel<MoimingGroupResponseDTO> read(String uuid) {
        return null;
    }


    @Override
    public TransferModel<MoimingGroupResponseDTO> update(@RequestBody TransferModel<MoimingGroupRequestDTO> request) {
        return null;
    }


    @Override
    public TransferModel delete(String uuid) {
        return null;
    }


    // MoimingMembersDTO를 보내는 과정.
    @GetMapping("/getGroupMembers/{groupUuid}")
    public TransferModel<List<MoimingMembersDTO>> giveGroupMembers(@PathVariable String groupUuid) {

        Optional<MoimingGroup> optional = groupRepository.findById(UUID.fromString(groupUuid));

        List<MoimingMembersDTO> responseData = new ArrayList<>();

        if (optional.isPresent()) {

            MoimingGroup thisGroup = optional.get();
            List<UserGroupLinker> linkerList = thisGroup.getGroupUserList();

            // 링커를 통해 User 하나하나 추출
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
            // 추출한 유저를 통해 GroupMemberDTO 형성
            // list 화 해서 전송.

            return TransferModel.OK(responseData);


        } else {

            // 잘못된 그룹 정보.
            return TransferModel.ERROR(404, "존재하지 않는 명령입니다. 시도하지 말아주세요.");
        }

    }

    @PostMapping("/getGroupSessions/{groupUuid}/{notificationCheck}")
    public TransferModel<List<SessionAndUserStatusDTO>> requestGroupSessions(@PathVariable String groupUuid, @RequestBody String userUuid
            , @PathVariable Integer notificationCheck) { // curUserUuid 도 넣어서.

        String thisUserUuid = userUuid.replace("\"", "");
        System.out.println("1: " + groupUuid + "\n2: " + thisUserUuid);

        boolean isNotiCheckNeeded = false;
        if (notificationCheck == 1) isNotiCheckNeeded = true;

        List<SessionAndUserStatusDTO> sessionAndStatusList = new ArrayList<>();
        List<MoimingSession> sessionList = sessionRepository.findByMoimingGroupUuid(UUID.fromString(groupUuid)).get();

        // 이 그룹이 가지고 있는 모든 세션들
        for (MoimingSession session : sessionList) {

            SessionAndUserStatusDTO singleDTO = new SessionAndUserStatusDTO(); // 만들어 나가는 것

            singleDTO.setMoimingSession(session);

            // 1. 내가 주인인가? 0 = 내 정산
            if (session.getSessionCreatorUuid().toString().equals(thisUserUuid)) {

                singleDTO.setCurUserStatus(0);

            } else { // 내가 주인이 아닌 정산

                // 내가 주인이 아닌 정산의 주인을 불러온다.
                MoimingUser creator = userRepository.findById(session.getSessionCreatorUuid()).get();
                singleDTO.setCreatorName(creator.getUserName());

                // 이그룹의 각각의 세션들과 무슨 관계인지 확인한다.
                Optional<UserSessionLinker> requestLinker = usLinkerRepository.findByMoimingSessionUuidAndMoimingUserUuid(session.getUuid(), UUID.fromString(thisUserUuid));

                if (requestLinker.isPresent()) {

                    UserSessionLinker curUserLinker = requestLinker.get();

                    singleDTO.setCurUserCost(curUserLinker.getPersonalCost()); // 이 유저가 내야 하는 금액

                    if (curUserLinker.isSent()) {
                        singleDTO.setCurUserStatus(2); // 송금 완료했다고 Set

                    } else {
                        // Notification (지금 보는 유저 -> 이 세션에게, Type 2 의 요청을 보낸적이 있는지)
                        // TODO: 여기서 보내는 Notification 들에 대해선 isRead 바꿔줘도 될듯?

                        Optional<Notification> preNoti = notiRepository.findBySentUserUuidAndSentSessionUuidAndMsgType(UUID.fromString(thisUserUuid), session.getUuid(), 2);

                        if (preNoti.isPresent()) { // 송금 확인 요청을 보낸 상태임
                            singleDTO.setCurUserStatus(3);
                        } else { // 송금 확인 요청을 보내지 않은 정산임 TODO: 송금필요
                            singleDTO.setCurUserStatus(1);
                        }
                    }
                } else { // 미참여하는 정산활동.

                    singleDTO.setCurUserStatus(4);
                }

            }
            sessionAndStatusList.add(singleDTO);
        }

        boolean isNotiChanged = false;
        // 여기까지 왔으면 해당 그룹에 있는 알림들을 모두 읽음처리해준다.
        if (isNotiCheckNeeded) {

            isNotiChanged = checkUserGroupNotification(thisUserUuid, groupUuid);
        }

        if(isNotiChanged)return TransferModel.OK(sessionAndStatusList, "changed");
        else return TransferModel.OK(sessionAndStatusList);

    }

    private boolean checkUserGroupNotification(String userUuid, String groupUuid) {

        Optional<List<Notification>> findNotiList = notiRepository.findByMoimingUserUuidAndSentGroupUuid(UUID.fromString(userUuid)
                , UUID.fromString(groupUuid));

        List<Notification> notiList = findNotiList.get();

        boolean isNotiChanged = false;

        for (int i = 0; i < notiList.size(); i++) {

            Notification thisNoti = notiList.get(i);

            if(thisNoti.getSentActivity().equals("group") && !(thisNoti.getIsRead())){

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

            MoimingGroup thisGroup = isGroupPresent.get(); // 새로 가져온 그룹.

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

        // 링커를 통해 User 하나하나 추출
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

        //TODO: 1. 그룹 생성
        MoimingGroupRequestDTO groupRequest = requestDto.getGroupRequest();

        MoimingGroup creatingGroup = MoimingGroup.builder()
                .groupName(groupRequest.getGroupName())
                .groupInfo(groupRequest.getGroupInfo())
                .groupCreatorUuid(groupRequest.getGroupCreatorUuid())
                .bgImg(groupRequest.getBgImg())
                .groupMemberCnt(groupRequest.getGroupMemberCnt())
                .createdAt(LocalDateTime.now().withNano(0))
                .build();

        MoimingGroup savedGroup = groupRepository.save(creatingGroup);

        //TODO: 2. 연결자 생성, 생성자 포함해서 다같이 List 에 포함되어 있기 때문에 한꺼번에 돌려도 됨.
        List<UUID> membersUuidList = requestDto.getMembersUuidList();
        List<MoimingMembersDTO> responseMemberList = new ArrayList<>();

        for (int i = 0; i < membersUuidList.size(); i++) {

            Optional<MoimingUser> findUser = userRepository.findById(membersUuidList.get(i));
            MoimingUser groupMember = findUser.get();

            //UserGroupLinking 을 진행해준다.
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

        }

        //TODO: 3. 세션 형성
        MoimingSessionRequestDTO sessionRequest = requestDto.getSessionRequest();

        MoimingSession createSesssion = MoimingSession.builder()
                .sessionCreatorUuid(savedGroup.getGroupCreatorUuid())
                .moimingGroup(savedGroup)
                .sessionType(sessionRequest.getSessionType())
                .sessionName(sessionRequest.getSessionName())
                .sessionMemberCnt(sessionRequest.getSessionMemberCnt())
                .curSenderCnt(1)  // 총무는 보냈을 것.
                .totalCost(sessionRequest.getTotalCost())
                .singleCost(sessionRequest.getSingleCost())
                .curCost(sessionRequest.getSessionCreatorCost()) // 총무가 보낸 돈
                .isFinished(false)
                .createdAt(LocalDateTime.now().withNano(0))
                .build();

        MoimingSession savedSession = sessionRepository.save(createSesssion);


        //TODO: 4. 멤버 세션 연결
        //TODO: 5. 비멤버 세션 연결
        List<UserSessionLinkerRequestDTO> usDataList = requestDto.getUsDataList();

        for (int i = 0; i < usDataList.size(); i++) {

            UserSessionLinkerRequestDTO requestData = usDataList.get(i);

            if (requestData.getIsMoimingUser()) { // 모이밍 멤버일경우 USLinkLogic 으로

                if (i == 0) { //TODO? 이걸 i=0 으로 판단하는게 맞나..?
                    createUserLinker(requestData, true, savedSession);
                } else {
                    createUserLinker(requestData, false, savedSession);
                }

            } else { // 아닐경우 NMULinkLogic 으로

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
    public TransferModel<MoimingGroupResponseDTO> groupNoticeRequest(@RequestBody TransferModel<GroupNoticeDTO> requestModel){


        return moimingGroupLogicService.groupNoticeRequest(requestModel.getData());

    }
}
