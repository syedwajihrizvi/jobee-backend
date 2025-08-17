package com.rizvi.jobee.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.rizvi.jobee.dtos.UserAccountSummaryDto;
import com.rizvi.jobee.dtos.UserProfileSummaryDto;
import com.rizvi.jobee.entities.UserAccount;
import com.rizvi.jobee.entities.UserProfile;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserAccountSummaryDto toSummaryDto(UserAccount userAccount);

    @Mapping(target = "profileComplete", expression = """
            java(userProfile.getDocuments() != null &&
            !userProfile.getDocuments().isEmpty() &&
            userProfile.getTitle() != null &&
            !userProfile.getTitle().isEmpty() &&
            userProfile.getSummary() != null &&
            !userProfile.getSummary().isEmpty())""")
    UserProfileSummaryDto toProfileSummaryDto(UserProfile userProfile);
}
