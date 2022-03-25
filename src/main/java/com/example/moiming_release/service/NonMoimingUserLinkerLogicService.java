package com.example.moiming_release.service;

import com.example.moiming_release.model.entity.MoimingSession;
import com.example.moiming_release.model.entity.NonMoimingUser;
import com.example.moiming_release.model.network.request.UserSessionLinkerRequestDTO;
import com.example.moiming_release.repository.MoimingSessionRepository;
import com.example.moiming_release.repository.NonMoimingUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NonMoimingUserLinkerLogicService {

    @Autowired
    private MoimingSessionRepository sessionRepository;

    @Autowired
    private NonMoimingUserRepository nmuRepository;

    public boolean create(UserSessionLinkerRequestDTO singleRequest) {

        MoimingSession linkedSession = sessionRepository.getOne(singleRequest.getSessionUuid());

        NonMoimingUser nmu = NonMoimingUser.builder()
                .nmuName(singleRequest.getUserName())
                .isNmuSent(false)
                .moimingSession(linkedSession)
                .nmuPersonalCost(singleRequest.getPersonalCost())
                .createdAt(LocalDateTime.now().withNano(0))
                .build();


        NonMoimingUser nmuCreated = nmuRepository.save(nmu);

        return true;

    }

}
