package com.example.moiming_release.controller.api;

import com.example.moiming_release.controller.intf.CrudInterface;
import com.example.moiming_release.model.network.TransferModel;
import com.example.moiming_release.model.network.request.GroupPaymentRequestDTO;
import com.example.moiming_release.model.network.response.GroupPaymentResponseDTO;
import com.example.moiming_release.model.other.PaymentAndSenderDTO;
import com.example.moiming_release.service.GroupPaymentLogicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment")
public class GroupPaymentController {

    @Autowired
    private GroupPaymentLogicService groupPaymentLogicService;

    @PostMapping("/create")
    public TransferModel<GroupPaymentResponseDTO> create(@RequestBody TransferModel<PaymentAndSenderDTO> requestModel) {

        return groupPaymentLogicService.create(requestModel);
    }

    @GetMapping("/{uuid}")
    public TransferModel<List<GroupPaymentResponseDTO>> read(@PathVariable String uuid) {
        return groupPaymentLogicService.read(uuid);
    }

    @PutMapping("/update/{uuid}")
    public TransferModel<GroupPaymentResponseDTO> update(@RequestBody TransferModel<PaymentAndSenderDTO> request, @PathVariable String uuid) {

        return groupPaymentLogicService.update(request, uuid);
    }

    @PostMapping("/delete")
    public TransferModel delete(@RequestBody TransferModel<List<String>> requestModel) {
        return groupPaymentLogicService.delete(requestModel);
    }
}
