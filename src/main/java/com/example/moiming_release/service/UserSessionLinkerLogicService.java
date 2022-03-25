package com.example.moiming_release.service;

import com.example.moiming_release.repository.*;
import com.example.moiming_release.model.entity.*;
import com.example.moiming_release.model.network.TransferModel;
import com.example.moiming_release.model.network.request.UserSessionLinkerRequestDTO;
import com.example.moiming_release.model.network.response.NonMoimingUserResponseDTO;
import com.example.moiming_release.model.network.response.UserSessionLinkerResponseDTO;
import com.example.moiming_release.model.other.MoimingMembersDTO;
import com.example.moiming_release.model.other.SessionMembersDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserSessionLinkerLogicService {

    @Autowired
    private UserSessionLinkerRepository linkerRepository;

    @Autowired
    private MoimingUserRepository userRepository;

    @Autowired
    private MoimingSessionRepository sessionRepository;

    @Autowired
    private NonMoimingUserRepository nmuRepository;

    @Autowired
    private NotificationRepository notiRepository;


    public boolean create(UserSessionLinkerRequestDTO singleRequest, boolean isCreator) {

        MoimingUser linkedUser = userRepository.getOne(singleRequest.getMoimingUserUuid());
        MoimingSession linkedSession = sessionRepository.getOne(singleRequest.getSessionUuid());

        UserSessionLinker insertLinker = UserSessionLinker.builder()
                .personalCost(singleRequest.getPersonalCost())
                .moimingUser(linkedUser)
                .moimingSession(linkedSession)
                .createdAt(LocalDateTime.now().withNano(0))
                .build();

        if (isCreator) {
            insertLinker.setSent(true);
        } else {
            insertLinker.setSent(false);
        }

        UserSessionLinker linked = linkerRepository.save(insertLinker);

        return true;
    }


    public TransferModel<SessionMembersDTO> read(String userUuid, String sessionUuid) {

        MoimingSession curSession = sessionRepository.findById(UUID.fromString(sessionUuid)).get();
        MoimingUser userCreator = userRepository.findById(curSession.getSessionCreatorUuid()).get();

        //1. SessionUUID 로 연결되어 있는 유저들 가져와서 UserSessionLinker 들 반환
        List<UserSessionLinker> membersDataList = linkerRepository.findByMoimingSession_Uuid(UUID.fromString(sessionUuid)).get();
        List<NonMoimingUser> nmuDataList = nmuRepository.findByMoimingSession_Uuid(UUID.fromString(sessionUuid)).get();

        //2. 해당 List들을 토대로 ResponseDTO List 들을 각각 만든다.
        List<UserSessionLinkerResponseDTO> memberLinkerDTO = new ArrayList<>();

        for (int i = 0; i < membersDataList.size(); i++) {

            UserSessionLinker data = membersDataList.get(i);

            UserSessionLinkerResponseDTO linkerDTO = UserSessionLinkerResponseDTO.builder()
                    .isSent(data.isSent())
                    .personalCost(data.getPersonalCost())
                    .createdAt(data.getCreatedAt())
                    .updatedAt(data.getUpdatedAt())
                    .moimingUser(data.getMoimingUser())
                    .build();

            memberLinkerDTO.add(linkerDTO);
        }

        List<NonMoimingUserResponseDTO> nmuLinkerDTO = new ArrayList<>();

        for (int i = 0; i < nmuDataList.size(); i++) {

            NonMoimingUser nmuData = nmuDataList.get(i);

            NonMoimingUserResponseDTO nmuDTO = NonMoimingUserResponseDTO.builder()
                    .isNmuSent(nmuData.isNmuSent())
                    .nmuName(nmuData.getNmuName())
                    .nmuPersonalCost(nmuData.getNmuPersonalCost())
                    .createdAt(nmuData.getCreatedAt())
                    .updatedAt(nmuData.getUpdatedAt())
                    .uuid(nmuData.getUuid())
                    .build();

            nmuLinkerDTO.add(nmuDTO);

        }

        MoimingMembersDTO sessionCreatorInfo
                = MoimingMembersDTO.builder()
                .uuid(userCreator.getUuid())
                .oauthUid(userCreator.getOauthUid())
                .userName(userCreator.getUserName())
                .userPfImg(userCreator.getUserPfImg())
                .bankName(userCreator.getBankName())
                .bankNumber(userCreator.getBankNumber())
                .build();


        //3. SessionMembersDTO 에 담아서 보낸다.
        SessionMembersDTO responseData = SessionMembersDTO.builder()
                .sessionCreatorInfo(sessionCreatorInfo)
                .sessionMoimingMemberList(memberLinkerDTO)
                .sessionNmuList(nmuLinkerDTO)
                .build();


        boolean isNotiChanged = checkUserSessionNotification(userUuid, sessionUuid);

        if (isNotiChanged) return TransferModel.OK(responseData, "changed");
        else return TransferModel.OK(responseData);

    }


    private boolean checkUserSessionNotification(String userUuid, String sessionUuid) {

        Optional<List<Notification>> findNotiList = notiRepository.findByMoimingUserUuidAndSentSessionUuid(UUID.fromString(userUuid)
                , UUID.fromString(sessionUuid));

        List<Notification> notiList = findNotiList.get();

        boolean isNotiChanged = false;

        for (int i = 0; i < notiList.size(); i++) {

            Notification thisNoti = notiList.get(i);
            if (!thisNoti.getIsRead()) {
                thisNoti.setIsRead(true);

                notiRepository.save(thisNoti);

                isNotiChanged = true;
            }
        }

        return isNotiChanged;
    }
}
