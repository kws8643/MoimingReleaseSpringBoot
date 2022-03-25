package com.example.moiming_release.service;

import java.util.UUID;

public class SharedClass {

    public static String buildUuid() {

        UUID uuid = UUID.randomUUID();

        // "-" 제외
        String givenUuid = UUID.randomUUID().toString().replace("-", "");

        return givenUuid;
    }
}
