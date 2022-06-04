package com.example.moiming_release.service;


import com.example.moiming_release.controller.api.AppNoticeController;
import com.example.moiming_release.controller.api.NotificationController;
import com.example.moiming_release.model.entity.AppNotice;
import com.example.moiming_release.model.network.TransferModel;
import com.example.moiming_release.model.network.request.AppNoticeRequestDTO;
import com.example.moiming_release.model.network.request.NotificationRequestDTO;
import com.example.moiming_release.model.network.response.AppNoticeResponseDTO;
import com.example.moiming_release.repository.AppNoticeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AppNoticeService {

    @Autowired
    private AppNoticeRepository appNoticeRepository;

    @Autowired
    private NotificationController notificationController;

    public String create(TransferModel<AppNoticeRequestDTO> requestModel) {

        AppNoticeRequestDTO appNoticeDto = requestModel.getData();

        AppNotice appNotice = AppNotice.builder()
                .noticeTitle(appNoticeDto.getNoticeTitle())
                .noticeInfo(appNoticeDto.getNoticeInfo())
                .isUrlLinked(appNoticeDto.getIsUrlLinked())
                .noticeUrl(appNoticeDto.getNoticeUrl())
                .isOpen(true)
                .createdAt(LocalDateTime.now().withNano(0))
                .build();

        AppNotice savedNotice = appNoticeRepository.save(appNotice);

        // SAVED NOTICE 가지고 모든 유저에 대하여 Notification 을 생성한다.

        NotificationRequestDTO sendNotification
                = NotificationRequestDTO.builder()
                .sentActivity("system")
                .msgType(1)
                .msgText(savedNotice.getNoticeInfo())
                .build();

        try {

            notificationController.createSystemNotification(sendNotification);

            return "Successful";

        } catch (Exception e) {

            return e.getMessage();
        }
    }


    public TransferModel<List<AppNoticeResponseDTO>> getAppNotice(String userUuid) {

        // userUuid 로는 판별할게 있나..?
        // authentication token 으로 할까?

        Optional<List<AppNotice>> findNoticeList = appNoticeRepository.findByIsOpen(true);

        List<AppNoticeResponseDTO> responseList = new ArrayList<>();

        if (findNoticeList.isPresent()) {

            List<AppNotice> noticeList = findNoticeList.get();

            for (AppNotice notice : noticeList) {

                // Open 된 값들만 보내므로 Open 여부는 필요 없다.
                AppNoticeResponseDTO respNotice = AppNoticeResponseDTO.builder()
                        .uuid(notice.getUuid())
                        .noticeTitle(notice.getNoticeTitle())
                        .noticeInfo(notice.getNoticeInfo())
                        .isUrlLinked(notice.getIsUrlLinked())
                        .noticeUrl(notice.getNoticeUrl())
                        .createdAt(notice.getCreatedAt())
                        .updatedAt(notice.getUpdatedAt())
                        .build();

                responseList.add(respNotice);
            }


            return TransferModel.OK(responseList);

        } else {

            // 발견된 열린 공지사항이 없습니다.

            return TransferModel.ERROR(403, "App Notice Not Found");
        }


    }


}
