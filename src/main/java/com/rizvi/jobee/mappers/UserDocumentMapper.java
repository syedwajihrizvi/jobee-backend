package com.rizvi.jobee.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.rizvi.jobee.dtos.UserDocumentDto;
import com.rizvi.jobee.entities.UserDocument;

@Mapper(componentModel = "spring")
public interface UserDocumentMapper {

    @Mapping(target = "documentUrl", source = "documentUrl")
    @Mapping(target = "filename", expression = "java(userDocument.getFileName())")
    public abstract UserDocumentDto toDto(UserDocument userDocument);
}
