package com.example.moiming_release.service;

import com.example.moiming_release.auth.jwt.JwtTokenProvider;
import com.example.moiming_release.controller.intf.CrudInterface;
import com.example.moiming_release.model.entity.MoimingUser;
import com.example.moiming_release.model.network.TransferModel;
import com.example.moiming_release.model.network.request.MoimingUserRequestDTO;
import com.example.moiming_release.model.network.response.MoimingUserResponseDTO;
import com.example.moiming_release.repository.MoimingUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class MoimingUserLogicService implements CrudInterface<MoimingUserRequestDTO, MoimingUserResponseDTO> {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private MoimingUserRepository userRepository;

    @Override
    public TransferModel<MoimingUserResponseDTO> create(TransferModel<MoimingUserRequestDTO> request) {

        MoimingUserRequestDTO requestedUser = request.getData();

        MoimingUser addUser = MoimingUser.builder()
                .oauthUid(requestedUser.getOauthUid())
                .oauthType(requestedUser.getOauthType())
                .userName(requestedUser.getUserName())
                .userEmail(requestedUser.getUserEmail())
                .userPfImg(requestedUser.getUserPfImg())
                .phoneNumber(requestedUser.getPhoneNumber())
                .createdAt(LocalDateTime.now().withNano(0))
                .build();

        MoimingUser savedUser = userRepository.save(addUser);

        return response(savedUser);

    }

    @Override
    public TransferModel<MoimingUserResponseDTO> read(String uuid) {

        Optional<MoimingUser> findCurUser = userRepository.findById(UUID.fromString(uuid));

        if (findCurUser.isPresent()) {

            MoimingUser curUser = findCurUser.get();

            return response(curUser);

        } else {

            return TransferModel.ERROR(400, "User Data Not Found Error");
        }
    }

    @Override
    public TransferModel<MoimingUserResponseDTO> update(TransferModel<MoimingUserRequestDTO> request) {

        MoimingUserRequestDTO updatedInfo = request.getData();
        Optional<MoimingUser> getPreUser = userRepository.findById(updatedInfo.getUserUuid());

        if (!getPreUser.isEmpty()) {

            MoimingUser preUser = getPreUser.get();

            preUser.setBankName(updatedInfo.getBankName());
            preUser.setBankNumber(updatedInfo.getBankNumber());
            preUser.setUpdatedAt(LocalDateTime.now().withNano(0));

            userRepository.save(preUser);

            return response(preUser);

        } else {

            return TransferModel.ERROR(400, "User Data Not Found Error");
        }


    }

    @Override
    public TransferModel delete(String uuid) {
        return null;
    }


    // 신규 유저를 생성시에 반환하는 함수.
    public TransferModel<MoimingUserResponseDTO> response(MoimingUser savedUser) {

        String newUserToken = jwtTokenProvider.createToken(savedUser.getUuid().toString());

        MoimingUserResponseDTO responseUser = MoimingUserResponseDTO.builder()
                .uuid(savedUser.getUuid())
                .oauthUid(savedUser.getOauthUid())
                .oauthType(savedUser.getOauthType())
                .userName(savedUser.getUserName())
                .userEmail(savedUser.getUserEmail())
                .phoneNumber(savedUser.getPhoneNumber())
                .userPfImg(savedUser.getUserPfImg())
                .bankName(savedUser.getBankName())
                .bankNumber(savedUser.getBankNumber())
                .createdAt(savedUser.getCreatedAt())
                .updatedAt(savedUser.getUpdatedAt())
                .build();

        return TransferModel.OK(newUserToken, responseUser); //유저 등록에 대한 반환.
    }
}
