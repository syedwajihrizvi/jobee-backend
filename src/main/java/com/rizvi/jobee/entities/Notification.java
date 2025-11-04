package com.rizvi.jobee.entities;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.rizvi.jobee.enums.MessagerUserType;
import com.rizvi.jobee.enums.NotificationType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recepient_id", nullable = false)
    private Long recepientId;

    @Column(name = "recepient_type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private MessagerUserType recepientType;

    @Column(name = "notification_type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private NotificationType notificationType;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Column(name = "created_ad", nullable = true, updatable = false)
    @CreationTimestamp
    private LocalDateTime timestamp;

    @Column(name = "read", nullable = false)
    @Builder.Default
    private Boolean read = false;

}
