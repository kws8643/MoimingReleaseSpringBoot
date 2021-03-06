package com.example.moiming_release.service;


import com.example.moiming_release.model.entity.*;
import com.example.moiming_release.model.network.TransferModel;
import com.example.moiming_release.model.network.request.GroupPaymentRequestDTO;
import com.example.moiming_release.model.network.response.GroupPaymentResponseDTO;
import com.example.moiming_release.model.other.PaymentAndSenderDTO;
import com.example.moiming_release.repository.GroupPaymentRepository;
import com.example.moiming_release.repository.MoimingGroupRepository;
import com.example.moiming_release.repository.MoimingUserRepository;
import com.example.moiming_release.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class GroupPaymentLogicService {

    @Autowired
    private MoimingUserRepository userRepository;

    @Autowired
    private GroupPaymentRepository paymentRepository;

    @Autowired
    private MoimingGroupRepository groupRepository;

    @Autowired
    private NotificationRepository notiRepository;


    public TransferModel<GroupPaymentResponseDTO> create(TransferModel<PaymentAndSenderDTO> request) {

        UUID sentUserUuid = request.getData().getSentUserUuid();
        Optional<MoimingUser> findSentUser = userRepository.findById(sentUserUuid);

        GroupPaymentRequestDTO requestDTO = request.getData().getPaymentData();

        Optional<MoimingGroup> findGroup = groupRepository.findById(requestDTO.getGroupUuid());
        MoimingGroup thisGroup = findGroup.get();

        GroupPayment groupPayment = GroupPayment.builder()
                .paymentName(requestDTO.getPaymentName())
                .paymentDate(LocalDate.parse(requestDTO.getPaymentDate()))
                .paymentCost(requestDTO.getPaymentCost())
                .moimingGroup(thisGroup)
                .paymentType(requestDTO.isPaymentType())
                .createdAt(LocalDateTime.now().withNano(0))
                .build();

        GroupPayment savedPayment = paymentRepository.save(groupPayment);

        // ?????? ????????? Payment ??? ????????? Notification ??? ??? ??????????????? ????????? ????????????
        List<UserGroupLinker> groupMembers = thisGroup.getGroupUserList();

        String msg = findSentUser.get().getUserName() + "?????? " + thisGroup.getGroupName() + "??? ?????????????????? "
                + savedPayment.getPaymentName() + "??? ???????????????";

        for (int i = 0; i < groupMembers.size(); i++) {

            MoimingUser memberUser = groupMembers.get(i).getMoimingUser();

            if (!memberUser.getUuid().toString().equals(findSentUser.get().getUuid().toString())) {

                Notification noti = Notification.builder()
                        .sentUserUuid(sentUserUuid)
                        .sentActivity("group")
                        .sentGroupUuid(thisGroup.getUuid())
                        .msgType(2)
                        .msgText(msg)
                        .isRead(false)
                        .moimingUser(memberUser)
                        .createdAt(LocalDateTime.now().withNano(0))
                        .build();

                notiRepository.save(noti);
            }
        }


        // TODO: ?????? ????????? ????????? ??????????????????
        int preGroupPaymentCost = thisGroup.getGroupPayment();
        int paymentCost = savedPayment.getPaymentCost();

        if (savedPayment.isPaymentType()) {// ??????

            preGroupPaymentCost += paymentCost;
        } else {

            preGroupPaymentCost -= paymentCost;
        }

        thisGroup.setGroupPayment(preGroupPaymentCost);
        thisGroup.setUpdatedAt(LocalDateTime.now().withNano(0));

        groupRepository.save(thisGroup);

        // TODO
//        sendNotification(sentUserUuid, savedPayment);

        return response(savedPayment);

    }


    public TransferModel<List<GroupPaymentResponseDTO>> read(String uuid) {

        List<GroupPaymentResponseDTO> responseList = new ArrayList<>();

        Optional<MoimingGroup> findGroup = groupRepository.findById(UUID.fromString(uuid));
        MoimingGroup targetGroup = findGroup.get();
        List<GroupPayment> paymentList = targetGroup.getPaymentList();

        for (GroupPayment groupPayment : paymentList) {

            GroupPaymentResponseDTO singleResponse = GroupPaymentResponseDTO.builder()
                    .uuid(groupPayment.getUuid())
                    .paymentName(groupPayment.getPaymentName())
                    .paymentCost(groupPayment.getPaymentCost())
                    .paymentDate(groupPayment.getPaymentDate().toString())
                    .paymentType(groupPayment.isPaymentType())
                    .createdAt(groupPayment.getCreatedAt())
                    .updatedAt(groupPayment.getUpdatedAt())

                    .build();

            responseList.add(singleResponse);
        }

        return TransferModel.OK(responseList);

    }


    public TransferModel<GroupPaymentResponseDTO> update(TransferModel<PaymentAndSenderDTO> request, String paymentUuid) {

        Optional<GroupPayment> findData = paymentRepository.findById(UUID.fromString(paymentUuid));

        PaymentAndSenderDTO receivedData = request.getData();
        GroupPaymentRequestDTO updateInfo = receivedData.getPaymentData();

        Optional<MoimingUser> findSentUser = userRepository.findById(receivedData.getSentUserUuid());
        MoimingUser sentUser = findSentUser.get();

        if (findData.isPresent()) {

            GroupPayment paymentData = findData.get();
            MoimingGroup paymentGroup = paymentData.getMoimingGroup();

            int preGroupPaymentCost = paymentGroup.getGroupPayment();
            int paymentCost = paymentData.getPaymentCost();

            if (paymentData.isPaymentType()) {// ??????????????????

                preGroupPaymentCost -= paymentCost;
            } else {

                preGroupPaymentCost += paymentCost;
            }

            // ?????? ?????? payment ??? ?????????

            paymentData.setPaymentName(updateInfo.getPaymentName())
                    .setPaymentDate(LocalDate.parse(updateInfo.getPaymentDate()))
                    .setPaymentCost(updateInfo.getPaymentCost())
                    .setPaymentType(updateInfo.isPaymentType())
                    .setUpdatedAt(LocalDateTime.now().withNano(0));

            // ??????????????? ????????? ?????? ??????
            GroupPayment savedPayment = paymentRepository.save(paymentData);

            // ?????? ????????? Payment ??? ????????? Notification ??? ??? ??????????????? ????????? ????????????
            List<UserGroupLinker> groupMembers = paymentGroup.getGroupUserList();

            String msg = sentUser.getUserName() + "?????? " + paymentGroup.getGroupName() + "??? ?????????????????? "
                    + savedPayment.getPaymentName() + "??? ???????????????";

            for (int i = 0; i < groupMembers.size(); i++) {

                MoimingUser memberUser = groupMembers.get(i).getMoimingUser();

                if (!memberUser.getUuid().toString().equals(receivedData.getSentUserUuid().toString())) {

                    Notification noti = Notification.builder()
                            .sentUserUuid(receivedData.getSentUserUuid())
                            .sentActivity("group")
                            .sentGroupUuid(paymentGroup.getUuid())
                            .msgType(2)
                            .msgText(msg)
                            .isRead(false)
                            .moimingUser(memberUser)
                            .createdAt(LocalDateTime.now().withNano(0))
                            .build();

                    notiRepository.save(noti);
                }
            }


            if (savedPayment.isPaymentType()) {// ??????

                preGroupPaymentCost += paymentCost;
            } else {

                preGroupPaymentCost -= paymentCost;
            }

            paymentGroup.setGroupPayment(preGroupPaymentCost);
            paymentGroup.setUpdatedAt(LocalDateTime.now().withNano(0));

            groupRepository.save(paymentGroup);

            return response(savedPayment);

        } else {

            return TransferModel.ERROR(401, "Payment Not Found Error");
        }
    }


    public TransferModel delete(TransferModel<List<String>> requestModel) {

        String paymentUuid = requestModel.getData().get(0);
        String sentUserUuid = requestModel.getData().get(1);

        Optional<MoimingUser> findSentUser = userRepository.findById(UUID.fromString(sentUserUuid));
        MoimingUser sentUser = findSentUser.get();

        Optional<GroupPayment> findData = paymentRepository.findById(UUID.fromString(paymentUuid));

        if (findData.isPresent()) {

            GroupPayment paymentData = findData.get();
            String paymentName = paymentData.getPaymentName();

            paymentRepository.delete(paymentData); // DB ?????? ??????

            MoimingGroup paymentGroup = paymentData.getMoimingGroup();

            int preGroupPaymentCost = paymentGroup.getGroupPayment();
            int paymentCost = paymentData.getPaymentCost();

            // ?????????
            if (paymentData.isPaymentType()) {// ??????????????????

                preGroupPaymentCost -= paymentCost;
            } else {

                preGroupPaymentCost += paymentCost;
            }

            paymentGroup.setGroupPayment(preGroupPaymentCost);
            paymentGroup.setUpdatedAt(LocalDateTime.now().withNano(0));

            groupRepository.save(paymentGroup);

            List<UserGroupLinker> groupMembers = paymentGroup.getGroupUserList();

            String msg = sentUser.getUserName() + "?????? " + paymentGroup.getGroupName() + "??? ?????????????????? "
                    + paymentName + "??? ???????????????";

            for (int i = 0; i < groupMembers.size(); i++) {

                MoimingUser memberUser = groupMembers.get(i).getMoimingUser();

                if (!memberUser.getUuid().toString().equals(sentUser.getUuid().toString())) {

                    Notification noti = Notification.builder()
                            .sentUserUuid(sentUser.getUuid())
                            .sentActivity("group")
                            .sentGroupUuid(paymentGroup.getUuid())
                            .msgType(2)
                            .msgText(msg)
                            .isRead(false)
                            .moimingUser(memberUser)
                            .createdAt(LocalDateTime.now().withNano(0))
                            .build();

                    notiRepository.save(noti);
                }
            }


        } else {

            return TransferModel.ERROR(401, "Payment Not Found Error");
        }

        return TransferModel.OK();
    }

    // ????????? ?????? ??????
    public TransferModel<GroupPaymentResponseDTO> response(GroupPayment savedPayment) {

        GroupPaymentResponseDTO responsePayment = GroupPaymentResponseDTO.builder()
                .uuid(savedPayment.getUuid())
                .paymentName(savedPayment.getPaymentName())
                .paymentCost(savedPayment.getPaymentCost())
                .paymentDate(savedPayment.getPaymentDate().toString())
                .paymentType(savedPayment.isPaymentType())
                .createdAt(savedPayment.getCreatedAt())
                .updatedAt(savedPayment.getUpdatedAt())
                .build();

        return TransferModel.OK(responsePayment);

    }


    private void sendNotification(UUID sentUserUuid, UUID sentGroupUuid, GroupPayment savedPayment) { // ?????? ???????????? ??????,?????? ??????????????? ??????

        // TODO: Group UUID  ???????????? ??????????????? ?????? ?????????.
/**        private UUID sentUserUuid;
 private String sentActivity;
 private UUID sentGroupUuid;
 private UUID sentSessionUuid;
 private Integer msgType;
 private String msgText;
 private Boolean isRead;**/

        //TODO: NOTI ????????? ????????? ??? ????????????! --> ??????????????? ????????? ????????????,????????? FCM?????? ????????? ???????????? ?????? ?????? ????????? ??????.
        //      ??? ???????????? user ????????? ????????? ????????? ???.
        //      ex) ~~ ?????? ~~????????? ????????? ?????? (?????? / ??????) ???????????????.
/*

        Notification notiItem = Notification.builder()
                .
                .build();

*/

//        notiRepository.save(notiItem);

    }


}
