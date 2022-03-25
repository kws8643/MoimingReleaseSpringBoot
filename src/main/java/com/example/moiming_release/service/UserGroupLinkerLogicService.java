package com.example.moiming_release.service;

import com.example.moiming_release.model.entity.MoimingGroup;
import com.example.moiming_release.model.entity.MoimingUser;
import com.example.moiming_release.model.entity.UserGroupLinker;
import com.example.moiming_release.model.network.TransferModel;
import com.example.moiming_release.model.network.request.UserGroupLinkerRequestDTO;
import com.example.moiming_release.model.network.response.UserGroupLinkerResponseDTO;
import com.example.moiming_release.model.other.MoimingGroupAndMembersDTO;
import com.example.moiming_release.model.other.MoimingMembersDTO;
import com.example.moiming_release.repository.MoimingGroupRepository;
import com.example.moiming_release.repository.MoimingUserRepository;
import com.example.moiming_release.repository.UserGroupLinkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserGroupLinkerLogicService {

    @Autowired
    private UserGroupLinkerRepository linkerRepository;

    @Autowired
    private MoimingUserRepository userRepository;

    @Autowired
    private MoimingGroupRepository groupRepository;


    public TransferModel<UserGroupLinkerResponseDTO> create(TransferModel<UserGroupLinkerRequestDTO> request) {

        UserGroupLinkerRequestDTO requestDTO = request.getData();
        Map<Integer, UUID> membersUuid = requestDTO.getMembersUuid();

        for (int i = 0; i < membersUuid.size(); i++) {

            UUID user = membersUuid.get(i);
            MoimingUser test = userRepository.getOne(user);

            System.out.println("1: " + user);

            System.out.println("2: " + requestDTO.getGroupUuid());

            UserGroupLinker groupLinker = UserGroupLinker.builder()
                    .moimingUser(userRepository.getOne(user))
                    .moimingGroup(groupRepository.getOne(requestDTO.getGroupUuid()))
                    .createdAt(LocalDateTime.now().withNano(0))
                    .build();


            linkerRepository.save(groupLinker);
        }


        return TransferModel.OK();
    }

    public TransferModel<List<UserGroupLinker>> read(String uuid) {

        Optional<MoimingUser> requestUser = userRepository.findById(UUID.fromString(uuid));

        List<UserGroupLinker> ugLinkerList = requestUser.get().getUserGroupList();

        return TransferModel.OK(ugLinkerList);
    }



    // 음.... N^2 이라서 no use

    public TransferModel<List<UserGroupLinkerResponseDTO>> hi(String uuid) {

        Optional<List<UserGroupLinker>> isLinkerPresent =  linkerRepository.findByMoimingUserUuid(UUID.fromString(uuid));

        List<UserGroupLinkerResponseDTO> transferData = new ArrayList<>();

        if(isLinkerPresent.isPresent()) {

            List<UserGroupLinker> linkerList = isLinkerPresent.get();

            for (int i = 0; i < linkerList.size(); i++) {

                UserGroupLinker linkerData = linkerList.get(i);

                MoimingGroup group = linkerData.getMoimingGroup();

                MoimingGroupAndMembersDTO singleGroupData = MoimingGroupAndMembersDTO.builder()
                        .moimingGroupDto(group)
                        .moimingMembersList(searchGroupMembers(group))
                        .build();

                UserGroupLinkerResponseDTO singleGroupLinkerData = UserGroupLinkerResponseDTO.builder()
                        .id(linkerData.getId())
                        .moimingGroupAndMembersDTO(singleGroupData)
                     /*   .noticeCnt(linkerData.getNoticeCnt())
                        .recentNotice(linkerData.getRecentNotice())*/
                        .createdAt(linkerData.getCreatedAt())
                        .updatedAt(linkerData.getUpdatedAt())
                        .build();

                transferData.add(singleGroupLinkerData);

            }

            return TransferModel.OK(transferData);

        }else{

            return TransferModel.OK();
        }

    }

    private List<MoimingMembersDTO> searchGroupMembers(MoimingGroup group){

            List<MoimingMembersDTO> groupMembersDTO = new ArrayList<>();
            List<UserGroupLinker> linkerList = group.getGroupUserList();

            // 링커를 통해 User 하나하나 추출
            for (UserGroupLinker linker : linkerList) {

                MoimingUser member = linker.getMoimingUser();
                MoimingMembersDTO memberDTO = MoimingMembersDTO.builder()
                        .uuid(member.getUuid())
                        .oauthUid(member.getOauthUid())
                        .userName(member.getUserName())
                        .userPfImg(member.getUserPfImg())
                        .bankName(member.getBankName())
                        .bankNumber(member.getBankNumber())
                        .build();

                groupMembersDTO.add(memberDTO);
            }

            return groupMembersDTO;
    }


    public TransferModel<UserGroupLinkerResponseDTO> update(TransferModel<UserGroupLinkerRequestDTO> request) {
        return null;
    }

    public TransferModel delete(String uuid) {
        return null;
    }


}
