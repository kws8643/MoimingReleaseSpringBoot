package com.example.moiming_release.service.auth;


import com.example.moiming_release.exception.CCommunicationException;
import com.example.moiming_release.model.auth.KakaoAuthModel;
import com.google.gson.Gson;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


// 받아온 Access Token을 통해서 카카오에게 전달하여 해당 유저의 정보를 가져오는 곳.
// 해당 유저의 OAuth UID를 통해서 우리 앱의 유저인지 판단하게 된다.

@Service
public class KakaoAuthService {

    public KakaoAuthModel getKakaoProfile(String accessToken) {
        // Set header : Content-type: application/x-www-form-urlencoded
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Bearer " + accessToken);

        RestTemplate restTemplate = new RestTemplate();
        Gson gson = new Gson();

        // Set http entity
        HttpEntity<String> requestInfo = new HttpEntity<>("", headers);


        try {
            // Request profile
            ResponseEntity<String> response = restTemplate
                    .postForEntity("https://kapi.kakao.com/v2/user/me", requestInfo, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {

                System.out.println(response.getBody());

                return gson.fromJson(response.getBody(), KakaoAuthModel.class);

            }else{

                System.out.println("no");

            }
        } catch (Exception e) {
            throw new CCommunicationException();
        }
        throw new CCommunicationException();
    }


}