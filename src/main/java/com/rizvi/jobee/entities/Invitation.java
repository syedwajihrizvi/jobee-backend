package com.rizvi.jobee.entities;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.rizvi.jobee.enums.BusinessType;
import com.rizvi.jobee.enums.InvitationStatus;

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
@Table(name = "invitations", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "email", "phone_number", "company_code" })
})
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = true, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "status")
    @Enumerated(value = EnumType.STRING)
    @Builder.Default
    private InvitationStatus status = InvitationStatus.PENDING;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number", nullable = true)
    private String phoneNumber;

    @Column(name = "role")
    @Enumerated(value = EnumType.STRING)
    private BusinessType invitationType;

    @Column(name = "company_code", nullable = false, unique = true)
    private String companyCode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "invited_by", nullable = false)
    private BusinessAccount invitedBy;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

}
