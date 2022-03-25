package com.example.moiming_release.controller.api;


import com.example.moiming_release.model.entity.UserGroupLinker;
import com.example.moiming_release.model.network.TransferModel;
import com.example.moiming_release.model.network.request.UserSessionLinkerRequestDTO;
import com.example.moiming_release.model.network.response.UserSessionLinkerResponseDTO;
import com.example.moiming_release.model.other.SessionMembersDTO;
import com.example.moiming_release.service.NonMoimingUserLinkerLogicService;
import com.example.moiming_release.service.UserSessionLinkerLogicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/userSessionLinker")
public class UserSessionLinkerController {

    @Autowired
    NonMoimingUserLinkerLogicService nmuLinkerLogicService;

    @Autowired
    UserSessionLinkerLogicService usLinkerLogicService;

    // Session 을 형성할때 Moiming, Nmu 요청이 한꺼번에 들어와서, 내부적으로 파싱 후 생성 컨트롤러로 넘긴다.
    @PostMapping("/receiveAll")
    private TransferModel<List<Boolean>> create(@RequestBody TransferModel<List<UserSessionLinkerRequestDTO>> request) {

        System.out.println(request);

        List<UserSessionLinkerRequestDTO> requestList = request.getData();

        List<Boolean> responseList = new ArrayList<>();

        int j =0 ;

        for(UserSessionLinkerRequestDTO dto : requestList){
            System.out.println("[" + j++ + "]: " + dto.toString() + "\n");
        }

        for (int i = 0; i < requestList.size(); i++) {

            UserSessionLinkerRequestDTO requestData = requestList.get(i);

            if (requestData.getIsMoimingUser()) { // 모이밍 멤버일경우 USLinkLogic 으로

                if (i == 0) {
                    responseList.add(usLinkerLogicService.create(requestData, true));
                } else {
                    responseList.add(usLinkerLogicService.create(requestData, false));
                }

            } else { // 아닐경우 NMULinkLogic 으로

                responseList.add(nmuLinkerLogicService.create(requestData));

            }

        }

        for (int i = 0; i < responseList.size(); i++) {

            if (responseList.get(i)) {
                return TransferModel.ERROR(200, i + "user not added");
            }
        }

        return TransferModel.OK();
    }

    // SessionActivity 진입 시, 해당하는 Session 에 속한 모든 유저를 가져와주는 Class
    @GetMapping("/{userUuid}/{sessionUuid}")
    public TransferModel<SessionMembersDTO> read(@PathVariable String userUuid, @PathVariable String sessionUuid) {


        return usLinkerLogicService.read(userUuid,sessionUuid);

    }
}
