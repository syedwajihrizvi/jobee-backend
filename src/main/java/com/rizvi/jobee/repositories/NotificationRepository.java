package com.rizvi.jobee.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rizvi.jobee.entities.Notification;
import com.rizvi.jobee.enums.MessagerUserType;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientIdAndRecipientType(Long recipientId, MessagerUserType recipientType);
}
