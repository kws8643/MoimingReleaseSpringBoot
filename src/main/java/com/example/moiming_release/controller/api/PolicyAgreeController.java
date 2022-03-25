package com.example.moiming_release.controller.api;

import com.example.moiming_release.model.entity.MoimingUser;
import com.example.moiming_release.model.entity.PolicyAgree;
import com.example.moiming_release.model.network.TransferModel;
import com.example.moiming_release.model.network.request.PolicyAgreeRequestDTO;
import com.example.moiming_release.model.other.UserAndPolicyAgreeDTO;
import com.example.moiming_release.repository.MoimingUserRepository;
import com.example.moiming_release.repository.PolicyAgreeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/policy")
public class PolicyAgreeController {

    @Autowired
    private MoimingUserRepository userRepository;

    @Autowired
    private PolicyAgreeRepository policyRepository;


    @PostMapping("/linkWithUser")
    public TransferModel<String> createAgreedPolicy(@RequestBody TransferModel<UserAndPolicyAgreeDTO> requestModel) {

        UserAndPolicyAgreeDTO chunckData = requestModel.getData();

        List<PolicyAgreeRequestDTO> userAgreeList = chunckData.getUserAgreeList();

        Optional<MoimingUser> getUser = userRepository.findById(chunckData.getUserUuid());

        if (getUser.isPresent()) {

            for (int i = 0; i < userAgreeList.size(); i++) {

                PolicyAgreeRequestDTO requestPolicy = userAgreeList.get(i);

                PolicyAgree userAgreedPolicy = PolicyAgree.builder()
                        .policyNumber(requestPolicy.getPolicyNumber())
                        .policyInfo(requestPolicy.getPolicyInfo())
                        .isAgreed(requestPolicy.getIsAgreed())
                        .moimingUser(getUser.get())
                        .createdAt(LocalDate.now())
                        .build();

                policyRepository.save(userAgreedPolicy);

            }

            // 연결 완료
            return TransferModel.OK("Successful!");


        } else {

            return TransferModel.ERROR(500, "Submitted User Is Not Found");
        }


    }

}
