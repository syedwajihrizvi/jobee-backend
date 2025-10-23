package com.rizvi.jobee.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.rizvi.jobee.dtos.message.MessageDto;
import com.rizvi.jobee.entities.Message;
import com.rizvi.jobee.enums.MessagerUserType;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    @Mapping(target = "sentByUser", expression = "java(message.messageSentByUserWithType(userId, userRole))")
    @Mapping(target = "conversationId", expression = "java(message.getConversation().getId())")
    MessageDto toMessageDto(Message message, Long userId, MessagerUserType userRole);
}
