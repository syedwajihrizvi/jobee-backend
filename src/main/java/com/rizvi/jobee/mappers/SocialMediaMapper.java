package com.rizvi.jobee.mappers;

import org.mapstruct.Mapper;

import com.rizvi.jobee.dtos.socialMedia.SocialMediaDto;
import com.rizvi.jobee.entities.BusinessSocial;
import com.rizvi.jobee.entities.Social;

@Mapper(componentModel = "spring")
public interface SocialMediaMapper {
    SocialMediaDto toSocialMediaDto(Social social);

    SocialMediaDto toSocialMediaDto(BusinessSocial businessSocial);
}
