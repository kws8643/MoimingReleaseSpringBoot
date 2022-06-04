package com.example.moiming_release.service.auth;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

public class GetFcmAccessToken {

    private static final String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String[] SCOPES = {MESSAGING_SCOPE};

    public static String getAccessToken() throws IOException {

        File file = ResourceUtils.getFile("classpath:service-account.json");

        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new FileInputStream(file))
                .createScoped(Arrays.asList(SCOPES));
//        googleCredentials.refreshAccessToken();

        googleCredentials.refresh();

        AccessToken at = googleCredentials.getAccessToken();
        String value = at.getTokenValue();

        return value;

    }

}
