package com.example.moiming_release.service;

import com.example.moiming_release.controller.intf.CrudInterface;
import com.example.moiming_release.model.entity.MoimingSession;
import com.example.moiming_release.model.entity.NonMoimingUser;
import com.example.moiming_release.model.entity.Notification;
import com.example.moiming_release.model.entity.UserSessionLinker;
import com.example.moiming_release.model.network.TransferModel;
import com.example.moiming_release.model.network.request.MoimingSessionRequestDTO;
import com.example.moiming_release.model.network.response.MoimingSessionResponseDTO;
import com.example.moiming_release.model.other.SessionStatusChangeDTO;
import com.example.moiming_release.repository.*;
import org.hibernate.cache.spi.support.EntityReadOnlyAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MoimingSessionLogicService implements CrudInterface<MoimingSessionRequestDTO, MoimingSessionResponseDTO> {

    @Autowired
    MoimingSessionRepository sessionRepository;

    @Autowired
    MoimingGroupRepository groupRepository;

    @Autowired
    UserSessionLinkerRepository usLinkerRepository;

    @Autowired
    NotificationRepository notiRepository;

    @Autowired
    NonMoimingUserRepository nmuRepository;

    @Override
    public TransferModel<MoimingSessionResponseDTO> create(TransferModel<MoimingSessionRequestDTO> request) {

        MoimingSessionRequestDTO requestedSession = request.getData();

        MoimingSession createSesssion = MoimingSession.builder().sessionCreatorUuid(requestedSession.getSessionCreatorUuid()).moimingGroup(groupRepository.getOne(requestedSession.getMoimingGroupUuid())).sessionType(requestedSession.getSessionType()).sessionName(requestedSession.getSessionName()).sessionMemberCnt(requestedSession.getSessionMemberCnt()).curSenderCnt(1)  // 총무는 보냈을 것.//
                .totalCost(requestedSession.getTotalCost()).singleCost(requestedSession.getSingleCost()).curCost(requestedSession.getSessionCreatorCost()) // 총무가 보낸 돈
                .isFinished(false).createdAt(LocalDateTime.now().withNano(0)).build();

        MoimingSession savedMoimingSession = sessionRepository.save(createSesssion);

        return createResponse(savedMoimingSession);
    }

    @Override
    public TransferModel<MoimingSessionResponseDTO> read(String uuid) {
        return null;
    }


    @Override
    public TransferModel<MoimingSessionResponseDTO> update(TransferModel<MoimingSessionRequestDTO> request) {
        return null;
    } // 사용 안할 예정


    // 세션 업데이트 요청에 대한 부분
    public TransferModel<MoimingSessionResponseDTO> updateStatus(TransferModel<SessionStatusChangeDTO> requestModel) {

        SessionStatusChangeDTO sessionData = requestModel.getData();

        List<UUID> unsentUser = sessionData.getUnsentUserList();
        List<UUID> unsentNmu = sessionData.getUnsentNmuList();
        List<UUID> sentUser = sessionData.getSentUserList();
        List<UUID> sentNmu = sessionData.getSentNmuList();

        Optional<MoimingSession> findSession = sessionRepository.findById(sessionData.getSessionUuid());

        if (findSession.isPresent()) {

            MoimingSession preSession = findSession.get();

            // 변수들 준비해놓자
            int curSenderCnt = preSession.getCurSenderCnt();
            int curCost = preSession.getCurCost();

            // TODO: 바꿔야 하는 것들 (일단 파싱하면서 다 바꿔야 함)
            //       1. 각 USLinker 및 NonMoimingUser 객체들
            //       2. 바꾸면서 적용할 Session Datat 들을 통해 MoimingSession 에 업데이트 한다.
            //       3. 각 정보들을 저장한다.

            for (int i = 0; i < unsentUser.size(); i++) { // sent 로 바꿔야 할 유저들
                UserSessionLinker linker = usLinkerRepository.findByMoimingSessionUuidAndMoimingUserUuid(preSession.getUuid(), unsentUser.get(i)).get();

                if (!linker.isSent()) {
                    linker.setSent(true);
                    linker.setUpdatedAt(LocalDateTime.now().withNano(0));

                    curSenderCnt++;
                    curCost += linker.getPersonalCost();

                    usLinkerRepository.save(linker);
                }

            }

            for (int i = 0; i < unsentNmu.size(); i++) { // sent 로 바꿔야 할 nmu 들

                NonMoimingUser nmu = nmuRepository.findByMoimingSessionUuidAndUuid(preSession.getUuid(), unsentNmu.get(i)).get();

                if (!nmu.isNmuSent()) {
                    nmu.setNmuSent(true);
                    nmu.setUpdatedAt(LocalDateTime.now().withNano(0));

                    curSenderCnt++;
                    curCost += nmu.getNmuPersonalCost();

                    nmuRepository.save(nmu);
                }

            }

            for (int i = 0; i < sentUser.size(); i++) { // unsent 로 바꿔야 할 유저들

                UserSessionLinker linker = usLinkerRepository.findByMoimingSessionUuidAndMoimingUserUuid(preSession.getUuid(), sentUser.get(i)).get();
                if (linker.isSent()) {
                    linker.setSent(false);
                    linker.setUpdatedAt(LocalDateTime.now().withNano(0));

                    curSenderCnt--;
                    curCost -= linker.getPersonalCost();

                    usLinkerRepository.save(linker);
                }
            }

            for (int i = 0; i < sentNmu.size(); i++) { // unsent 로 바꿔야 할 nmu 들
                NonMoimingUser nmu = nmuRepository.findByMoimingSessionUuidAndUuid(preSession.getUuid(), sentNmu.get(i)).get();

                if (nmu.isNmuSent()) {
                    nmu.setNmuSent(false);
                    nmu.setUpdatedAt(LocalDateTime.now().withNano(0));

                    curSenderCnt--;
                    curCost -= nmu.getNmuPersonalCost();

                    nmuRepository.save(nmu);
                }
            }

            // 마지막 수정사항 적용
            preSession.setCurCost(curCost);
            preSession.setCurSenderCnt(curSenderCnt);
            preSession.setUpdatedAt(LocalDateTime.now().withNano(0));

            if (preSession.getCurSenderCnt() == preSession.getSessionMemberCnt()) {
                preSession.setIsFinished(true);
            }

            MoimingSession postSession = sessionRepository.save(preSession);

            return createResponse(postSession);


        } else { // Session 을 찾지 못함.

            return TransferModel.ERROR(404, "요청하신 정산활동을 찾을 수 없습니다");
        }

    }


    @Override
    public TransferModel delete(String uuid) {
        return null;
    }

    public TransferModel<String> deleteSession(String sessionUuid){

        Optional<MoimingSession> findSession = sessionRepository.findById(UUID.fromString(sessionUuid));

        if(findSession.isPresent()){

            // 관계된 US Linker 삭제
            Optional<List<UserSessionLinker>> findLinkers = usLinkerRepository.findByMoimingSession_Uuid(UUID.fromString(sessionUuid));
            List<UserSessionLinker> usList = findLinkers.get();
            usLinkerRepository.deleteAll(usList);

            // 관계된 NMU 삭제
            Optional<List<NonMoimingUser>> findNmus = nmuRepository.findByMoimingSession_Uuid(UUID.fromString(sessionUuid));
            List<NonMoimingUser> nmuList = findNmus.get();
            nmuRepository.deleteAll(nmuList);

            // 관계된 Notification 삭제
            Optional<List<Notification>> findNotis = notiRepository.findBySentSessionUuid(UUID.fromString(sessionUuid));
            notiRepository.deleteAll(findNotis.get());

            // Session 삭제
            sessionRepository.delete(findSession.get());

            return TransferModel.OK();

        }else{

            return TransferModel.ERROR(404, "잘못된 요청입니다");

        }
    }

    private TransferModel<MoimingSessionResponseDTO> createResponse(MoimingSession savedSession) {

        MoimingSessionResponseDTO responseSessionDTO = MoimingSessionResponseDTO.builder()
                .uuid(savedSession.getUuid())
                .sessionCreatorUuid(savedSession.getSessionCreatorUuid())
                .sessionType(savedSession.getSessionType())
                .sessionName(savedSession.getSessionName())
                .sessionMemberCnt(savedSession.getSessionMemberCnt())
                .curSenderCnt(savedSession.getCurSenderCnt())
                .curCost(savedSession.getCurCost())
                .totalCost(savedSession.getTotalCost())
                .singleCost(savedSession.getSingleCost())
                .isFinished(savedSession.getIsFinished())
                .deletedAt(savedSession.getDeletedAt())
                .createdAt(savedSession.getCreatedAt())
                .updatedAt(savedSession.getUpdatedAt())
                .build();


        return TransferModel.OK(responseSessionDTO);

    }

}
