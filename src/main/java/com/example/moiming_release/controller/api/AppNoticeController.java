package com.example.moiming_release.controller.api;


import com.example.moiming_release.model.network.TransferModel;
import com.example.moiming_release.model.network.request.AppNoticeRequestDTO;
import com.example.moiming_release.model.network.request.NotificationRequestDTO;
import com.example.moiming_release.model.network.response.AppNoticeResponseDTO;
import com.example.moiming_release.service.AppNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appNotice")
public class AppNoticeController {

    @Autowired
    private AppNoticeService appNoticeService;


    @CrossOrigin(origins = "*")
    @PostMapping("/create")
    public String createNotification(@RequestBody TransferModel<AppNoticeRequestDTO> requestModel) {

        return appNoticeService.create(requestModel);
    }


    @GetMapping("/getAppNotice/{userUuid}")
    public TransferModel<List<AppNoticeResponseDTO>> getAppNotice(@PathVariable String userUuid) {

        return appNoticeService.getAppNotice(userUuid);
    }


}
