package com.rizvi.jobee.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rizvi.jobee.enums.UserDocumentType;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class UserDocumentService {
    private final S3Service s3Service;

    public boolean uploadDocument(
            Long userId, MultipartFile document, UserDocumentType documentType) {
        System.out.println(document);
        try {
            s3Service.uploadDocument(userId, document, documentType);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
