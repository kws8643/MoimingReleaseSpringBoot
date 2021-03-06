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

                    // ?????? ?????? ???????????????, ?????? ???????????? ???????????? ?????????, ??? ???????????? ?????? ????????? ????????? ?????? ???????????? ???????????? ??????.
                    Optional<MoimingUser> sentUser = userRepository.findById(request.getSentUserUuid());
                    Optional<MoimingSession> sentSession = sessionRepository.findById(request.getSentSessionUuid());

                    String msg = sentUser.get().getUserName() + "?????? " + sentSession.get().getSessionName() + " ????????? ???????????? ?????????! " +
                            "?????????????????? ????????? ???????????? ?????? ???????????????";

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

                    // TODO: ?????? ?????????????
                    sentSuccessUserUuid.add("R:" + toUser.get().getUuid().toString());

                }


            } else {
                isFailExist = true;
            }
        }


        if (!isFailExist) return TransferModel.OK(sentSuccessUserUuid);
        else return TransferModel.OK(); // ?????? ?????? ??????????

    }

    public TransferModel<List<ReceivedNotificationDTO>> findUserNotification(String activity, Integer msgType
            , String userUuid, String activityUuid) {

        List<ReceivedNotificationDTO> responseList = new ArrayList<>();

        // 1. User ??? ???????????? ????????????. (????????? ?????? ????????? ????????????) , (??? ????????? ?????? ??? ????????? ????????????)
        Optional<List<Notification>> findNotifications;

        if (activity.equals("system")) { // User uuid ??? ????????? ???, activity Uuid ??? ??????

            // ?????? ??????
            return TransferModel.OK(responseList);

        } else if (activity.equals("session")) { // ?????? ?????? ?????? ?????? ??????????????? ??????

            findNotifications = notiRepository.findByMoimingUserUuidAndSentSessionUuidAndMsgType(UUID.fromString(userUuid)
                    , UUID.fromString(activityUuid)
                    , msgType);

            if (findNotifications.isPresent()) {

                List<Notification> notificationVoList = findNotifications.get();

                for (Notification getNotification : notificationVoList) {

                    MoimingUser sentUser = userRepository.findById(getNotification.getSentUserUuid()).get();
                    MoimingSession sentSession = sessionRepository.findById(getNotification.getSentSessionUuid()).get();
                    String groupName = sentSession.getMoimingGroup().getGroupName();

                    // sentUser ??? ???????????? ????????? ??? ?????????.
                    UserSessionLinker usLinker = usLinkerRepository.findByMoimingSessionUuidAndMoimingUserUuid(sentSession.getUuid(), sentUser.getUuid()).get();

                    if (!usLinker.isSent()) { // ??? ????????? ???????????? ????????? ????????????.
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
            return TransferModel.OK(responseList); // ????????? ?????? ???
        }
        // 2.
        return TransferModel.ERROR(404, "Not Valid Request");
    }


    public TransferModel deleteNotification(String activity, Integer msgType, String sentUserUuid, String
            activityUuid) {

        if (activity.equals("session")) {
            if (msgType == 2) {
                // ?????? ?????? ?????? ????????? ?????? ?????? ??????
                Optional<Notification> findNoti = notiRepository.findBySentUserUuidAndSentSessionUuidAndMsgType(UUID.fromString(sentUserUuid)
                        , UUID.fromString(activityUuid)
                        , msgType);

                if (findNoti.isPresent()) {

                    notiRepository.delete(findNoti.get());

                    return TransferModel.OK();

                } else {

                    return TransferModel.ERROR(404, "???????????? ????????? ?????? ??? ????????????");
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
                } else { // system ?????? ?????? ?????? groupName, sessinName ??? ""

                    ReceivedNotificationDTO notiDto = ReceivedNotificationDTO.builder()
                            .notification(buildResponseDTO(noti))
                            .build();

                    responseList.add(notiDto);
                }
            }


        } else { // ???????????? ???.

            return TransferModel.ERROR(404, "????????? ???????????????");
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

        return response; //?????? ????????? ?????? ??????.
    }

}
