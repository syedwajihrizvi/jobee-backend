package com.rizvi.jobee.entities;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.rizvi.jobee.enums.MessagerUserType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "conversations", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "participant_one_id", "participant_two_id" })
})
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "participant_one_id", nullable = false)
    private Long participantOneId;

    @Column(name = "participant_two_id", nullable = false)
    private Long participantTwoId;

    @Column(name = "participant_one_type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private MessagerUserType participantOneType;

    @Column(name = "participant_two_type", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private MessagerUserType participantTwoType;

    @Column(name = "participant_one_name")
    private String participantOneName;

    @Column(name = "participant_two_name")
    private String participantTwoName;

    @Column(name = "last_message_id")
    private Long lastMessageId;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
