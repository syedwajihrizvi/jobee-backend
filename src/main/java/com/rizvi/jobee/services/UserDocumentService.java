package com.rizvi.jobee.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rizvi.jobee.enums.UserDocumentType;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class UserDocumentService {
    private final S3Service s3Service;

    public String uploadDocument(
            Long userId, MultipartFile document, UserDocumentType documentType) {
        try {
            var result = s3Service.uploadDocument(userId, document, documentType);
            return result;
        } catch (Exception e) {
            return null;
        }
    }
}
