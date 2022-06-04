package com.example.moiming_release.service;

import com.example.moiming_release.controller.intf.CrudInterface;
import com.example.moiming_release.model.entity.MoimingGroup;
import com.example.moiming_release.model.entity.MoimingUser;
import com.example.moiming_release.model.entity.UserGroupLinker;
import com.example.moiming_release.model.network.TransferModel;
import com.example.moiming_release.model.network.request.MoimingGroupRequestDTO;
import com.example.moiming_release.model.network.response.MoimingGroupResponseDTO;
import com.example.moiming_release.model.other.GroupNoticeDTO;
import com.example.moiming_release.model.other.MoimingGroupEditInfoDto;
import com.example.moiming_release.repository.MoimingGroupRepository;
import com.example.moiming_release.repository.MoimingUserRepository;
import com.example.moiming_release.repository.UserGroupLinkerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service // 항상 얘가 무슨 역할인지 Annotation 필수이다!!!!
public class MoimingGroupLogicService {

    @Autowired
    private MoimingGroupRepository moimingGroupRepository;

    @Autowired
    private UserGroupLinkerRepository ugLinkerRepository;

    @Autowired
    private MoimingUserRepository moimingUserRepository;

    public TransferModel<MoimingGroupResponseDTO> create(TransferModel<MoimingGroupRequestDTO> request) {

        MoimingGroupRequestDTO requestedGroup = request.getData();

        MoimingGroup addMoimingGroup = MoimingGroup.builder()
                .groupName(requestedGroup.getGroupName())
                .groupInfo(requestedGroup.getGroupInfo())
                .groupCreatorUuid(requestedGroup.getGroupCreatorUuid())
                .bgImg(requestedGroup.getBgImg())
                .groupMemberCnt(requestedGroup.getGroupMemberCnt())
                .groupPayment(0)
                .createdAt(LocalDateTime.now().withNano(0))
                .build();


        MoimingGroup savedMoimingGroup = moimingGroupRepository.save(addMoimingGroup);
        Optional<MoimingUser> findUser = moimingUserRepository.findById(requestedGroup.getGroupCreatorUuid());
        MoimingUser groupCreator = findUser.get();

        // 그룹을 만든 유저와 UserGroupLinking 을 진행해준다.
        UserGroupLinker ugLinker = UserGroupLinker.builder()
                .moimingGroup(savedMoimingGroup)
                .moimingUser(groupCreator)
                .createdAt(LocalDateTime.now().withNano(0))
//                .noticeCnt(0)
//                .recentNotice(null)
                .build();

        ugLinkerRepository.save(ugLinker);

        return response(savedMoimingGroup);

    }

    public TransferModel<MoimingGroupResponseDTO> update(TransferModel<MoimingGroupEditInfoDto> requestModel) {

        MoimingGroupEditInfoDto requestData = requestModel.getData();

        Optional<MoimingGroup> findGroup = moimingGroupRepository.findById(requestData.getGroupUuid());

        MoimingGroup editedGroup;

        if(findGroup.isPresent()){

            MoimingGroup editingGroup = findGroup.get();

            if(requestData.getGroupName().length() != 0){
                editingGroup.setGroupName(requestData.getGroupName());
            }

            if(requestData.getGroupInfo().length() != 0){
                editingGroup.setGroupInfo(requestData.getGroupInfo());
            }

            // TODO:::
            if(requestData.getGroupPfImg().length() != 0){
                editingGroup.setGroupPfImg(requestData.getGroupPfImg());
            }

            editingGroup.setUpdatedAt(LocalDateTime.now().withNano(0));

            editedGroup = moimingGroupRepository.save(editingGroup);
        }else{

            return TransferModel.ERROR(404, "");
        }


        return response(editedGroup);
    }

    public TransferModel delete(String uuid) {
        return null;
    }


    public TransferModel<MoimingGroupResponseDTO> groupNoticeRequest(GroupNoticeDTO noticeRequest) {

        Optional<MoimingGroup> findGroup = moimingGroupRepository.findById(noticeRequest.getGroupUuid());

        if (findGroup.isPresent()) {

            MoimingGroup thisGroup = findGroup.get();

            thisGroup.setNotice(noticeRequest.getNoticeInfo());
            thisGroup.setNoticeCreatorUuid(noticeRequest.getNoticeCreatorUuid());
            thisGroup.setNoticeCreatedAt(LocalDateTime.now().withNano(0));
            thisGroup.setUpdatedAt(LocalDateTime.now().withNano(0));

            MoimingGroup savedGroup = moimingGroupRepository.save(thisGroup);

            return response(savedGroup);

        } else {

            return TransferModel.ERROR(404, "No Group Found");

        }
    }


    public TransferModel<MoimingGroupResponseDTO> response(MoimingGroup savedMoimingGroup) {

        MoimingGroupResponseDTO responseGroupDTO = MoimingGroupResponseDTO.builder()
                .uuid(savedMoimingGroup.getUuid())
                .groupName(savedMoimingGroup.getGroupName())
                .groupInfo(savedMoimingGroup.getGroupInfo())
                .groupPfImg(savedMoimingGroup.getGroupPfImg())
                .groupMemberCnt(savedMoimingGroup.getGroupMemberCnt())
                .bgImg(savedMoimingGroup.getBgImg())
                .groupCreatorUuid(savedMoimingGroup.getGroupCreatorUuid())
                .groupPayment(savedMoimingGroup.getGroupPayment())
                .notice(savedMoimingGroup.getNotice())
                .noticeCreatorUuid(savedMoimingGroup.getNoticeCreatorUuid())
                .noticeCreatedAt(savedMoimingGroup.getNoticeCreatedAt())
                .createdAt(savedMoimingGroup.getCreatedAt())
                .updatedAt(savedMoimingGroup.getUpdatedAt())
                .build();

        return TransferModel.OK(responseGroupDTO);

    }

}