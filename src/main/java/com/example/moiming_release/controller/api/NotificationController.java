package com.example.moiming_release.controller.api;

import com.example.moiming_release.model.network.TransferModel;
import com.example.moiming_release.model.network.request.NotificationRequestDTO;
import com.example.moiming_release.model.network.response.NotificationResponseDTO;
import com.example.moiming_release.model.other.NotificationUserAndActivityDTO;
import com.example.moiming_release.model.other.ReceivedNotificationDTO;
import com.example.moiming_release.service.NotificationLogicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private NotificationLogicService notificationLogicService;

    @PostMapping("/create")
    public TransferModel<List<String>> createNotification(@RequestBody TransferModel<List<NotificationRequestDTO>> requestModel) {

        return notificationLogicService.create(requestModel);
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

}

