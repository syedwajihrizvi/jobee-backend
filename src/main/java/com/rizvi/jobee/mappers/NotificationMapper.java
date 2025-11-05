package com.rizvi.jobee.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.jpa.repository.EntityGraph;

import com.rizvi.jobee.dtos.notification.NotificationDto;
import com.rizvi.jobee.entities.Notification;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @EntityGraph(attributePaths = { "company", "application", "job" })
    @Mapping(target = "companyName", source = "company.name")
    @Mapping(target = "companyLogoUrl", source = "company.logo")
    @Mapping(target = "applicationId", source = "application.id")
    @Mapping(target = "jobId", source = "job.id")
    NotificationDto toNotificationDto(Notification notification);
}
