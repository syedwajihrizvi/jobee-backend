package com.rizvi.jobee.repositories;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.rizvi.jobee.entities.Notification;
import com.rizvi.jobee.enums.MessagerUserType;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientIdAndRecipientType(Long recipientId, MessagerUserType recipientType, Sort sort);

    @Modifying
    @Query("delete from Notification n where n.recipientId = :recipientId and n.recipientType = :recipientType and n.read = true")
    void deleteByRecipientIdAndRecipientTypeAndReadIsTrue(Long recipientId, MessagerUserType recipientType);
}
