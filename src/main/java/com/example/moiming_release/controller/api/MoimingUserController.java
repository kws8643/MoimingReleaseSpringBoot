package com.example.moiming_release.controller.api;

import com.example.moiming_release.controller.intf.CrudInterface;
import com.example.moiming_release.model.entity.MoimingUser;
import com.example.moiming_release.model.network.TransferModel;
import com.example.moiming_release.model.network.request.MoimingUserRequestDTO;
import com.example.moiming_release.model.network.response.MoimingUserResponseDTO;
import com.example.moiming_release.model.other.MoimingMembersDTO;
import com.example.moiming_release.repository.MoimingUserRepository;
import com.example.moiming_release.service.MoimingUserLogicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class MoimingUserController implements CrudInterface<MoimingUserRequestDTO, MoimingUserResponseDTO> {

    @Autowired
    private MoimingUserLogicService userLogicService;

    @Autowired
    private MoimingUserRepository userRepository;

    @Override
    @PostMapping("/signup")
    public TransferModel<MoimingUserResponseDTO> create(@RequestBody TransferModel<MoimingUserRequestDTO> request) {

        return userLogicService.create(request);

    }

    @Override
    @GetMapping("/{userUuid}")
    public TransferModel<MoimingUserResponseDTO> read(@PathVariable String userUuid) {
        return userLogicService.read(userUuid);
    }

    @Override
    @PostMapping("/updateUser")
    public TransferModel<MoimingUserResponseDTO> update(@RequestBody TransferModel<MoimingUserRequestDTO> request) {
        return userLogicService.update(request);
    }

    @Override
    public TransferModel delete(@PathVariable String uuid) {
        return null;
    }

    // 카카오 친구들 중 모이밍 유저 파싱하는 곳

    @PostMapping("/parseFromKakao")
    public TransferModel<List<MoimingMembersDTO>> parseKakaoUser(@RequestBody TransferModel<List<String>> kakaoFriends) {

        List<MoimingMembersDTO> parsedFriendsData = new ArrayList<>();
        List<String> kakaoFriendUids = kakaoFriends.getData();

        for (String uid : kakaoFriendUids) {

            Optional<MoimingUser> moimingFriend = userRepository.findByOauthUidAndAndOauthType(uid, "KAKAO");

            if (moimingFriend.isPresent()) {

                MoimingUser kakaoMoimingFriend = moimingFriend.get();

                MoimingMembersDTO groupMember = MoimingMembersDTO.builder()
                        .uuid(kakaoMoimingFriend.getUuid())
                        .oauthUid(kakaoMoimingFriend.getOauthUid())
                        .userName(kakaoMoimingFriend.getUserName())
                        .userPfImg(kakaoMoimingFriend.getUserPfImg())
                        .bankName(kakaoMoimingFriend.getBankName())
                        .bankNumber(kakaoMoimingFriend.getBankNumber())
                        .build();

                parsedFriendsData.add(groupMember);
            }
        }

        return TransferModel.OK(parsedFriendsData);

    }

}
