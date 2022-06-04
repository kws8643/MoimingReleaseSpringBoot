package com.example.moiming_release.controller.api;

import com.example.moiming_release.model.entity.MoimingUser;
import com.example.moiming_release.model.entity.Notification;
import com.example.moiming_release.model.network.TransferModel;
import com.example.moiming_release.model.network.request.NotificationRequestDTO;
import com.example.moiming_release.model.other.NotificationUserAndActivityDTO;
import com.example.moiming_release.model.other.ReceivedNotificationDTO;
import com.example.moiming_release.repository.MoimingUserRepository;
import com.example.moiming_release.repository.NotificationRepository;
import com.example.moiming_release.service.NotificationLogicService;
import com.example.moiming_release.service.auth.GetFcmAccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private MoimingUserRepository userRepository;

    @Autowired
    private NotificationLogicService notificationLogicService;

    @Autowired
    private NotificationRepository notiRepository;

    @PostMapping("/create")
    public TransferModel<List<String>> createNotification(@RequestBody TransferModel<List<NotificationRequestDTO>> requestModel) {

        return notificationLogicService.create(requestModel);
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/appToken")
    public String retrieveGoogleFcmToken() {

        try {

            String accToken = GetFcmAccessToken.getAccessToken();

            return accToken;

        } catch (IOException e) {

            String error = e.getMessage();
            System.out.println(error);

            return error;
        }
    }


    @PostMapping("/get/{activity}/{msgType}")
    public TransferModel<List<ReceivedNotificationDTO>> findUserNotification(@PathVariable String activity, @PathVariable String msgType
            , @RequestBody NotificationUserAndActivityDTO uuidInfo) {

        String userUuid = uuidInfo.getUserUuid().toString();
        String activityUuid = uuidInfo.getActivityUuid().toString();

        //TODO: UserUUID, SessionUUID 스트링 파싱 필요할 수도
        System.out.println("1: " + userUuid + "\n2:" + activityUuid);

        return notificationLogicService.findUserNotification(activity, Integer.parseInt(msgType), userUuid, activityUuid);
    }


    @PostMapping("/delete/{activity}/{msgType}")
    public TransferModel deleteNotification(@PathVariable String activity, @PathVariable String msgType
            , @RequestBody NotificationUserAndActivityDTO uuidInfo) {

        String sentUserUuid = uuidInfo.getSentUserUuid().toString();
        String activityUuid = uuidInfo.getActivityUuid().toString();

        return notificationLogicService.deleteNotification(activity, Integer.parseInt(msgType), sentUserUuid, activityUuid);
    }


    @PostMapping("/getAll")
    public TransferModel<List<ReceivedNotificationDTO>> getUserAllNotification(@RequestBody TransferModel<String> requestModel) {

        String userUuid = requestModel.getData();

        return (notificationLogicService.getUserAllNotification(userUuid));
    }


    //    @CrossOrigin(origins = "*")
//    @PostMapping("/createSystem")
    public String createSystemNotification(NotificationRequestDTO requestModel) {

        // 전원에게 저장한다.
        List<MoimingUser> findUser = userRepository.findAll();

        try {
            for (MoimingUser single : findUser) {

                Notification saveNoti = Notification.builder()
                        .sentActivity(requestModel.getSentActivity())
                        .msgType(requestModel.getMsgType())
                        .msgText(requestModel.getMsgText())
                        .isRead(false)
                        .moimingUser(single)
                        .createdAt(LocalDateTime.now().withNano(0))
                        .build();

                notiRepository.save(saveNoti);
            }

            return "Successful";

        } catch (Exception e) {

            return e.getMessage();
        }
    }


}

