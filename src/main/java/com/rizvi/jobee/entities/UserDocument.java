package com.rizvi.jobee.entities;

import java.time.LocalDateTime;

import com.rizvi.jobee.enums.UserDocumentType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_documents")
public class UserDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = true)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private UserDocumentType documentType;

    @Column(name = "document_url", nullable = false)
    private String documentUrl;

    @Column(name = "created_at", nullable = true, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "format_type", nullable = true)
    @Builder.Default
    private String formatType = "NON_IMG";

    @Column(name = "preview_url", nullable = true)
    private String previewUrl;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile user;

    public String getFileName() {
        if (documentUrl == null || !documentUrl.contains("/")) {
            return documentUrl;
        }

        int lastSlashIndex = documentUrl.lastIndexOf('/');
        int lastDotIndex = documentUrl.lastIndexOf('.');

        if (lastDotIndex == -1 || lastDotIndex < lastSlashIndex) {
            return documentUrl.substring(lastSlashIndex + 1);
        }

        return documentUrl.substring(lastSlashIndex + 1, lastDotIndex);
    }
}
