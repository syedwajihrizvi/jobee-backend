package com.rizvi.jobee.mappers;

import org.mapstruct.Mapper;

import com.rizvi.jobee.dtos.UserAccountSummaryDto;
import com.rizvi.jobee.dtos.UserProfileSummaryDto;
import com.rizvi.jobee.entities.UserAccount;
import com.rizvi.jobee.entities.UserProfile;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserAccountSummaryDto toSummaryDto(UserAccount userAccount);

    UserProfileSummaryDto toProfileSummaryDto(UserProfile userProfile);
}
