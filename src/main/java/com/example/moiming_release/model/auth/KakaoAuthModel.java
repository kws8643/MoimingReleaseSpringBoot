package com.example.moiming_release.model.auth;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

@Data
@ToString
public class KakaoAuthModel implements OAuth2User {

    private Long id;
    private Properties properties;
    private String email;

    @SerializedName("kakao_account")
    private KakaoAccount kakaoAccount;

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }


    @Data
    @ToString
    public class Properties{

        private String nickname;

        @SerializedName("thumbnail_image")
        private String thumbnailImage;

        @SerializedName("profile_image")
        private String profileImage;

    }

    @Data
    @ToString
    public class KakaoAccount{

        private String email;
        
    }
}

