package com.rizvi.jobee.mappers;

import org.mapstruct.Mapper;

import com.rizvi.jobee.dtos.UserDocumentDto;
import com.rizvi.jobee.entities.UserDocument;

@Mapper(componentModel = "spring")
public interface UserDocumentMapper {
    UserDocumentDto toDto(UserDocument userDocument);
}
