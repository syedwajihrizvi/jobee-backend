package com.rizvi.jobee.entities;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.rizvi.jobee.enums.MessagerUserType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "text", nullable = false, length = 1000)
    private String text;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Column(name = "timestamp", nullable = true, updatable = false)
    @CreationTimestamp
    private LocalDateTime timestamp;

    @Column(name = "read", nullable = false)
    @Builder.Default
    private Boolean read = false;

    @Column(name = "sender_type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private MessagerUserType senderType;

    @Column(name = "receiver_type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private MessagerUserType receiverType;

    @ManyToOne()
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    public Boolean messageSentByUser(Long userId, String userRole) {
        var userType = userRole.equals("BUSINESS") || userRole.equals("ADMIN") ? MessagerUserType.BUSINESS
                : MessagerUserType.USER;
        return this.senderId.equals(userId) && this.senderType == userType;
    }
}
