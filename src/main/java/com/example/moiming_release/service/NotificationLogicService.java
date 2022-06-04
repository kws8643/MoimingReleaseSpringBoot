package com.example.moiming_release.service;


import com.example.moiming_release.model.entity.*;
import com.example.moiming_release.model.network.TransferModel;
import com.example.moiming_release.model.network.request.NotificationRequestDTO;
import com.example.moiming_release.model.network.response.MoimingUserResponseDTO;
import com.example.moiming_release.model.network.response.NotificationResponseDTO;
import com.example.moiming_release.model.other.ReceivedNotificationDTO;
import com.example.moiming_release.repository.*;
import com.google.gson.annotations.SerializedName;
import org.aspectj.weaver.ast.Not;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Service Layer
@Service
public class NotificationLogicService {

    @Autowired
    private MoimingUserRepository userRepository;

    @Autowired
    private NotificationRepository notiRepository;

    @Autowired
    private MoimingSessionRepository sessionRepository;

    @Autowired
    private UserSessionLinkerRepository usLinkerRepository;

    @Autowired
    private MoimingGroupRepository groupRepository;

    public TransferModel<List<String>> create(TransferModel<List<NotificationRequestDTO>> requestModel) {

        boolean isFailExist = false;

        List<NotificationRequestDTO> requestList = requestModel.getData();
        List<String> sentSuccessUserUuid = new ArrayList<>();

        for (int i = 0; i < requestList.size(); i++) {

            NotificationRequestDTO request = requestList.get(i);

            Optional<List<Notification>> findNoti = notiRepository.findBySentSessionUuidAndMoimingUserUuidAndMsgType(request.getSentSessionUuid()
                    , request.getToUserUuid(), request.getMsgType());

            Optional<MoimingUser> toUser = userRepository.findById(request.getToUserUuid());

            List<Notification> notiList = findNoti.get();

            if (toUser.isPresent()) {

                if (notiList.size() == 0) {
                    Notification notification = Notification.builder()
                            .sentUserUuid(request.getSentUserUuid())
                            .sentActivity(request.getSentActivity())
                            .sentGroupUuid(request.getSentGroupUuid())
                            .sentSessionUuid(request.getSentSessionUuid())
                            .msgType(request.getMsgType())
                            .msgText(request.getMsgText())
                            .isRead(false)
                            .moimingUser(toUser.get())
                            .createdAt(LocalDateTime.now().withNano(0))
                            .build();

                    Notification savedNoti = notiRepository.save(notification);

                    sentSuccessUserUuid.add(toUser.get().getUuid().toString());

                } else {

                    // 이미 해당 정산활동과, 해당 유저간의 메시지가 있는데, 그 메시지가 송금 요청일 경우는 중복 메시지가 들어가게 된다.
                    Optional<MoimingUser> sentUser = userRepository.findById(request.getSentUserUuid());
                    Optional<MoimingSession> sentSession = sessionRepository.findById(request.getSentSessionUuid());

                    String msg = sentUser.get().getUserName() + "님이 " + sentSession.get().getSessionName() + " 정산을 기다리고 있어요! " +
                            "송금하셨다면 편하게 모이밍을 통해 알려주세요";

                    Notification notification = Notification.builder()
                            .sentUserUuid(request.getSentUserUuid())
                            .sentActivity(request.getSentActivity())
                            .sentGroupUuid(request.getSentGroupUuid())
                            .sentSessionUuid(request.getSentSessionUuid())
                            .msgType(request.getMsgType())
                            .msgText(msg)
                            .isRead(false)
                            .moimingUser(toUser.get())
                            .createdAt(LocalDateTime.now().withNano(0))
                            .build();

                    Notification savedNoti = notiRepository.save(notification);

                    // TODO: 겁나 이상한가?
                    sentSuccessUserUuid.add("R:" + toUser.get().getUuid().toString());

                }


            } else {
                isFailExist = true;
            }
        }


        if (!isFailExist) return TransferModel.OK(sentSuccessUserUuid);
        else return TransferModel.OK(); // 거의 없지 않을까?

    }

    public TransferModel<List<ReceivedNotificationDTO>> findUserNotification(String activity, Integer msgType
            , String userUuid, String activityUuid) {

        List<ReceivedNotificationDTO> responseList = new ArrayList<>();

        // 1. User 의 알림들을 가져온다. (전체일 경우 전체를 가져온다) , (타 활동일 경우 타 활동을 가져온다)
        Optional<List<Notification>> findNotifications;

        if (activity.equals("system")) { // User uuid 만 있으면 됨, activity Uuid 는 안옴

            // 이건 나중
            return TransferModel.OK(responseList);

        } else if (activity.equals("session")) { // 송금 요청 확인 알림 가져오려는 경우

            findNotifications = notiRepository.findByMoimingUserUuidAndSentSessionUuidAndMsgType(UUID.fromString(userUuid)
                    , UUID.fromString(activityUuid)
                    , msgType);

            if (findNotifications.isPresent()) {

                List<Notification> notificationVoList = findNotifications.get();

                for (Notification getNotification : notificationVoList) {

                    MoimingUser sentUser = userRepository.findById(getNotification.getSentUserUuid()).get();
                    MoimingSession sentSession = sessionRepository.findById(getNotification.getSentSessionUuid()).get();
                    String groupName = sentSession.getMoimingGroup().getGroupName();

                    // sentUser 가 보냈는지 확인해 줄 것이다.
                    UserSessionLinker usLinker = usLinkerRepository.findByMoimingSessionUuidAndMoimingUserUuid(sentSession.getUuid(), sentUser.getUuid()).get();

                    if (!usLinker.isSent()) { // 안 보냈을 경우에만 알림에 추가된다.
                        ReceivedNotificationDTO singleDto = ReceivedNotificationDTO.builder()
                                .sentUserName(sentUser.getUserName())
                                .sentSessionName(sentSession.getSessionName())
                                .sentGroupName(groupName)
                                .notification(buildResponseDTO(getNotification))
                                .build();

                        responseList.add(singleDto);
                    }
                }
            }
            return TransferModel.OK(responseList); // 없으면 빈게 감
        }
        // 2.
        return TransferModel.ERROR(404, "Not Valid Request");
    }


    public TransferModel deleteNotification(String activity, Integer msgType, String sentUserUuid, String
            activityUuid) {

        if (activity.equals("session")) {
            if (msgType == 2) {
                // 송금 요청 확인 알림에 대한 삭제 요청
                Optional<Notification> findNoti = notiRepository.findBySentUserUuidAndSentSessionUuidAndMsgType(UUID.fromString(sentUserUuid)
                        , UUID.fromString(activityUuid)
                        , msgType);

                if (findNoti.isPresent()) {

                    notiRepository.delete(findNoti.get());

                    return TransferModel.OK();

                } else {

                    return TransferModel.ERROR(404, "요청하신 알림을 찾을 수 없습니다");
                }


            }
        }

        return null;
    }

    public TransferModel<List<ReceivedNotificationDTO>> getUserAllNotification(String userUuid) {

        Optional<List<Notification>> findUserNoti = notiRepository.findByMoimingUserUuid(UUID.fromString(userUuid));
        List<ReceivedNotificationDTO> responseList = new ArrayList<>();

        if (findUserNoti.isPresent()) {

            List<Notification> notiList = findUserNoti.get();

            for (int i = 0; i < notiList.size(); i++) {

                Notification noti = notiList.get(i);


                if (noti.getSentUserUuid() != null) {

                    Optional<MoimingUser> sentUser = userRepository.findById(noti.getSentUserUuid());

                    if (noti.getSentActivity().equals("session")) {

                        Optional<MoimingSession> sentSession = sessionRepository.findById(noti.getSentSessionUuid());

                        ReceivedNotificationDTO notiDto = ReceivedNotificationDTO.builder()
                                .sentUserName(sentUser.get().getUserName())
                                .sentSessionName(sentSession.get().getSessionName())
                                .sentGroupName(sentSession.get().getMoimingGroup().getGroupName())
                                .notification(buildResponseDTO(noti))
                                .build();

                        responseList.add(notiDto);

                    } else if (noti.getSentActivity().equals("group")) {


                        Optional<MoimingGroup> sentGroup = groupRepository.findById(noti.getSentGroupUuid());

                        ReceivedNotificationDTO notiDto = ReceivedNotificationDTO.builder()
                                .sentUserName(sentUser.get().getUserName())
                                .sentSessionName("")
                                .sentGroupName(sentGroup.get().getGroupName())
                                .notification(buildResponseDTO(noti))
                                .build();

                        responseList.add(notiDto);

                    }
                } else { // system 에서 보낸 공지 groupName, sessinName 은 ""

                    ReceivedNotificationDTO notiDto = ReceivedNotificationDTO.builder()
                            .notification(buildResponseDTO(noti))
                            .build();

                    responseList.add(notiDto);
                }
            }


        } else { // 에러나는 것.

            return TransferModel.ERROR(404, "잘못된 요청입니다");
        }

        return TransferModel.OK(responseList);

    }

    public NotificationResponseDTO buildResponseDTO(Notification savedNoti) {

        NotificationResponseDTO response = NotificationResponseDTO.builder()
                .sentUserUuid(savedNoti.getSentUserUuid())
                .sentActivity(savedNoti.getSentActivity())
                .sentGroupUuid(savedNoti.getSentGroupUuid())
                .sentSessionUuid(savedNoti.getSentSessionUuid())
                .msgType(savedNoti.getMsgType())
                .msgText(savedNoti.getMsgText())
                .isRead(savedNoti.getIsRead())
                .createdAt(savedNoti.getCreatedAt())
                .build();

        return response; //유저 등록에 대한 반환.
    }

}
