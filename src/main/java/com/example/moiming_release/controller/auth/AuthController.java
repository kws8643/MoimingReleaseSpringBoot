package com.example.moiming_release.controller.auth;

import com.example.moiming_release.auth.jwt.JwtTokenProvider;
import com.example.moiming_release.model.entity.MoimingUser;
import com.example.moiming_release.model.auth.KakaoAuthModel;
import com.example.moiming_release.model.network.TransferModel;
import com.example.moiming_release.repository.MoimingUserRepository;
import com.example.moiming_release.service.auth.KakaoAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

//TODO: GET 방식의 통신이 맞는가? JWT를 반환하는 코딩 구조가 효율적인가? 별도의 Encryption 이 필요한 과정은 없는가?


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private MoimingUserRepository moimingUserRepository;

    @Autowired
    private KakaoAuthService kakaoAuthService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider; // 주입.


    /**
     * 그렇다면,
     * 토큰 기간이 만료되었다면 자동으로 새로 발급되어 돌려주고 로그인 시켜주는 방식 (로그인과의 merge)
     * 을 통해서 로그아웃이 안되는 것처럼 보이게 할 수도 있을 것같음.
     **/
    // 자동로그인 시도시 들어오는 주소. 가지고 온 토큰으로 로그인 가능한지 판단 후 결과 return.
    // TODO: 토큰 Verify 는 항상 해야하는 일. 따라서 따로 함수를 빼놓고 그것을 계속 실행하는 식으로 해야함.
    @GetMapping("/autoLogin")
    public TransferModel<Map<String, Object>> verifyExistingToken(@RequestHeader(value = "JwtToken") String jwtToken) {

        // 자동로그인 요청에 대해
        //1 . Validate 판단.
        boolean isValidate = jwtTokenProvider.validateToken(jwtToken);

        Map<String, Object> autoLoginInfo = new HashMap<>();
        autoLoginInfo.put("isValidate", isValidate);


        if (isValidate) { // 토큰은 유효하다

            System.out.println("토큰 유효. 유저 정보 전달.");

            //2 . 토큰 내 PrimeKey 를 통해 유저 확인 및 정보 전달.
            String userUuid = jwtTokenProvider.getUserPk(jwtToken);
            Optional<MoimingUser> findUser = moimingUserRepository.findById(UUID.fromString(userUuid)); //

            if (findUser.isPresent()) {

                MoimingUser targetUser = findUser.get();
                autoLoginInfo.put("isPresent", true);
                autoLoginInfo.put("loginUser", targetUser);

                return TransferModel.OK(autoLoginInfo);

            } else {
                // 이건 매우 이상한 상황
                // 토큰이 유효하지만 UUID가 없다?
                // No --> 계졍 삭제 후 다시 진입할 수도 있는 상황! 없으면 없다고 보내줘야 함.
                // 어쨌든 보내고, 회원가입으로 유도할 수 있게끔 Front 에서 처리
                autoLoginInfo.put("isPresent", false);
                return TransferModel.OK(autoLoginInfo);

            }

        } else { // 토큰이 만료되었다. 재로그인 유도하면 됨.

            System.out.println("유효하지 않은 토큰. 재로그인 필요.");

            autoLoginInfo.put("isPresent", false);
            return TransferModel.OK(autoLoginInfo);
        }

    }


    @GetMapping("/login")
    public TransferModel<Map<String, Object>> loginOrSignupUser(@RequestHeader(value = "KakaoAccessToken") String accessToken) {

        KakaoAuthModel kakaoModel = kakaoAuthService.getKakaoProfile(accessToken);

        String oauth_uid = String.valueOf(kakaoModel.getId());
        String oauth_type = "KAKAO";

        KakaoAuthModel.KakaoAccount accountInfo = kakaoModel.getKakaoAccount();
        KakaoAuthModel.Properties properties = kakaoModel.getProperties();

        String userEmail = accountInfo.getEmail();
        String userPfImg = properties.getProfileImage();

        Optional<MoimingUser> findUser = moimingUserRepository.findByOauthUidAndAndOauthType(oauth_uid, oauth_type);


        if (findUser.isPresent()) { // 해당 uid 의 유저가 존재한다 == 우리 회원임. (로그인 시킴/ Token 주기)

            MoimingUser targetUser = findUser.get();

            // 토큰에 Primary Key // 추후에 유저를 찾아줄 수 있도록 Pk로 생성
            String jwtToken = jwtTokenProvider.createToken(targetUser.getUuid().toString());

            Map<String, Object> loginInfo = new HashMap<>();
            loginInfo.put("isUser", true);
            loginInfo.put("loginUser", targetUser);


            return TransferModel.OK(jwtToken, loginInfo);


        } else { // 매치하는 유저가 없음. == 우리 회원 아님. (신규 가입으로 이동)

            System.out.println("user not found");

            Map<String, Object> signupInfo = new HashMap<>();
            signupInfo.put("isUser", false);
            signupInfo.put("oauthUid", oauth_uid);
            signupInfo.put("oauthEmail", userEmail);
            signupInfo.put("userPfImg", userPfImg);

            return TransferModel.OK(signupInfo);

        }

    }
}
