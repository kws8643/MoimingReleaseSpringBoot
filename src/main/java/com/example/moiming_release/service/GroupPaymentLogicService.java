package com.example.moiming_release.service;


import com.example.moiming_release.model.entity.GroupPayment;
import com.example.moiming_release.model.entity.MoimingGroup;
import com.example.moiming_release.model.entity.Notification;
import com.example.moiming_release.model.network.TransferModel;
import com.example.moiming_release.model.network.request.GroupPaymentRequestDTO;
import com.example.moiming_release.model.network.response.GroupPaymentResponseDTO;
import com.example.moiming_release.model.other.PaymentAndSenderDTO;
import com.example.moiming_release.repository.GroupPaymentRepository;
import com.example.moiming_release.repository.MoimingGroupRepository;
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
    private GroupPaymentRepository paymentRepository;

    @Autowired
    private MoimingGroupRepository groupRepository;

    @Autowired
    private NotificationRepository notiRepository;


    public TransferModel<GroupPaymentResponseDTO> create(TransferModel<PaymentAndSenderDTO> request) {

        UUID sentUserUuid = request.getData().getSentUserUuid();

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

        // TODO: 그룹 공금에 대해서 세팅해놓는다
        int preGroupPaymentCost = thisGroup.getGroupPayment();
        int paymentCost = savedPayment.getPaymentCost();

        if (savedPayment.isPaymentType()) {// 수입

            preGroupPaymentCost += paymentCost;
        }else {

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


    public TransferModel<GroupPaymentResponseDTO> update(TransferModel<GroupPaymentRequestDTO> request, String paymentUuid) {

        Optional<GroupPayment> findData = paymentRepository.findById(UUID.fromString(paymentUuid));

        GroupPaymentRequestDTO updateInfo = request.getData();

        if (findData.isPresent()) {

            GroupPayment paymentData = findData.get();
            MoimingGroup paymentGroup = paymentData.getMoimingGroup();

            int preGroupPaymentCost = paymentGroup.getGroupPayment();
            int paymentCost = paymentData.getPaymentCost();

            if (paymentData.isPaymentType()) {// 수입이였다면

                preGroupPaymentCost-= paymentCost;
            }else {

                preGroupPaymentCost += paymentCost;
            }

            // 이제 기존 payment 는 삭제함

            paymentData.setPaymentName(updateInfo.getPaymentName())
                    .setPaymentDate(LocalDate.parse(updateInfo.getPaymentDate()))
                    .setPaymentCost(updateInfo.getPaymentCost())
                    .setPaymentType(updateInfo.isPaymentType())
                    .setUpdatedAt(LocalDateTime.now().withNano(0));

            // 업데이트한 내용을 다시 저장
            GroupPayment savedPayment = paymentRepository.save(paymentData);

            if (savedPayment.isPaymentType()) {// 수입

                preGroupPaymentCost += paymentCost;
            }else {

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


    public TransferModel delete(String paymentUuid) {

        Optional<GroupPayment> findData = paymentRepository.findById(UUID.fromString(paymentUuid));

        if (findData.isPresent()) {

            GroupPayment paymentData = findData.get();

            paymentRepository.delete(paymentData); // DB 에서 깔끔하게 제거

            MoimingGroup paymentGroup = paymentData.getMoimingGroup();

            int preGroupPaymentCost = paymentGroup.getGroupPayment();
            int paymentCost = paymentData.getPaymentCost();

            // 반대로
            if (paymentData.isPaymentType()) {// 수입이였다면

                preGroupPaymentCost -= paymentCost;
            }else {

                preGroupPaymentCost += paymentCost;
            }

            paymentGroup.setGroupPayment(preGroupPaymentCost);
            paymentGroup.setUpdatedAt(LocalDateTime.now().withNano(0));

            groupRepository.save(paymentGroup);


        } else {

            return TransferModel.ERROR(401, "Payment Not Found Error");
        }

        return TransferModel.OK();
    }

    // 생성시 정달 함수
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


    private void sendNotification(UUID sentUserUuid, UUID sentGroupUuid, GroupPayment savedPayment){ // 그룹 페이먼트 정보,누가 수정하는지 정보

        // TODO: Group UUID  돌리면서 멤버들에게 모두 보내기.
/**        private UUID sentUserUuid;
        private String sentActivity;
        private UUID sentGroupUuid;
        private UUID sentSessionUuid;
        private Integer msgType;
        private String msgText;
        private Boolean isRead;**/

        //TODO: NOTI 내용에 대해서 쭉 정해놓기! --> 전체적으로 어떻게 정할건지,그리고 FCM에는 어떻게 실을건지 설계 후에 하는게 필요.
        //      다 넣을거면 user 찾아서 이름도 넣어야 함.
        //      ex) ~~ 님이 ~~그룹의 가계부 수정 (삭제 / 생성) 하였습니다.
/*

        Notification notiItem = Notification.builder()
                .
                .build();

*/

//        notiRepository.save(notiItem);

    }


}
