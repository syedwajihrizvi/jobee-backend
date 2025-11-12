package com.rizvi.jobee.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.jpa.repository.EntityGraph;

import com.rizvi.jobee.dtos.notification.CreateNotificationDto;
import com.rizvi.jobee.dtos.notification.NotificationDto;
import com.rizvi.jobee.entities.Interview;
import com.rizvi.jobee.entities.Notification;
import com.rizvi.jobee.enums.MessagerUserType;
import com.rizvi.jobee.enums.NotificationType;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    NotificationDto toNotificationDto(Notification notification);

    @Mapping(target = "recipientType", source = "recipientType")
    @Mapping(target = "applicationId", expression = "java(interview.getApplicationId())")
    @Mapping(target = "interviewId", source = "interview.id")
    @Mapping(target = "jobId", expression = "java(interview.getJobId())")
    @Mapping(target = "companyId", expression = "java(interview.getCompanyId())")
    @Mapping(target = "message", source = "message")
    @Mapping(target = "recipientId", expression = "java(interview.getCandidateId())")
    @Mapping(target = "notificationType", source = "notificationType")
    CreateNotificationDto toCreateNotificationDtoFromInterview(
            Interview interview, MessagerUserType recipientType,
            NotificationType notificationType, String message);
}
