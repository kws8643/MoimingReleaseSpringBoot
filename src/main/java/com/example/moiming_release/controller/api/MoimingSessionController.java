package com.example.moiming_release.controller.api;


import com.example.moiming_release.controller.intf.CrudInterface;
import com.example.moiming_release.model.entity.MoimingSession;
import com.example.moiming_release.model.network.TransferModel;
import com.example.moiming_release.model.network.request.MoimingSessionRequestDTO;
import com.example.moiming_release.model.network.response.MoimingSessionResponseDTO;
import com.example.moiming_release.model.other.SessionStatusChangeDTO;
import com.example.moiming_release.repository.MoimingSessionRepository;
import com.example.moiming_release.service.MoimingSessionLogicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/session")
public class MoimingSessionController implements CrudInterface<MoimingSessionRequestDTO, MoimingSessionResponseDTO> {

    @Autowired
    private MoimingSessionLogicService sessionLogicService;

    @Autowired
    private MoimingSessionRepository sessionRepository;

    @Override
    @PostMapping("/createSession")
    public TransferModel<MoimingSessionResponseDTO> create(@RequestBody TransferModel<MoimingSessionRequestDTO> request) {

        return sessionLogicService.create(request);
    }

    @Override
    public TransferModel<MoimingSessionResponseDTO> read(String uuid) {
        return null;
    }

    @Override
    public TransferModel<MoimingSessionResponseDTO> update(TransferModel<MoimingSessionRequestDTO> request) {
        return null;
    }

    @Override
    public TransferModel delete(String uuid) {
        return null;
    }


    @GetMapping("/getGroupFundings/{groupUuid}")
    public TransferModel<List<MoimingSessionResponseDTO>> readGroupFundings(@PathVariable String groupUuid) {

        List<MoimingSessionResponseDTO> responseData = new ArrayList<>();

        Optional<List<MoimingSession>> groupFundingData = sessionRepository.findByMoimingGroupUuidAndAndSessionType(UUID.fromString(groupUuid), 0);
        List<MoimingSession> groupFundingList = groupFundingData.get();

        for (MoimingSession sessionVO : groupFundingList) {

            MoimingSessionResponseDTO sessionDTO = MoimingSessionResponseDTO.builder()
                    .uuid(sessionVO.getUuid())
                    .sessionCreatorUuid(sessionVO.getSessionCreatorUuid())
                    .sessionType(sessionVO.getSessionType())
                    .sessionName(sessionVO.getSessionName())
                    .sessionMemberCnt(sessionVO.getSessionMemberCnt())
                    .curSenderCnt(sessionVO.getCurSenderCnt())
                    .totalCost(sessionVO.getTotalCost())
                    .singleCost(sessionVO.getSingleCost())
                    .isFinished(sessionVO.getIsFinished())
                    .deletedAt(sessionVO.getDeletedAt())
                    .createdAt(sessionVO.getCreatedAt())
                    .updatedAt(sessionVO.getUpdatedAt())
                    .nmuList(sessionVO.getNmuList())
                    .userSessionList(sessionVO.getUserSessionList())
                    .build();

            responseData.add(sessionDTO);
        }


        return TransferModel.OK(responseData);

    }

    @PutMapping("/updateSessionStatus")
    public TransferModel<MoimingSessionResponseDTO> updateSessionStatus(@RequestBody TransferModel<SessionStatusChangeDTO> requestModel) {

        return sessionLogicService.updateStatus(requestModel);
    }

    @PostMapping("/deleteSession")
    public TransferModel<String> deleteSession(@RequestBody TransferModel<String> requestModel) {

        String sessionUuid = requestModel.getData();

        return sessionLogicService.deleteSession(sessionUuid);

    }
}
