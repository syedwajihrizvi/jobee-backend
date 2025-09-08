package com.rizvi.jobee.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rizvi.jobee.entities.UserDocument;
import com.rizvi.jobee.entities.UserProfile;
import com.rizvi.jobee.enums.UserDocumentType;
import com.rizvi.jobee.repositories.UserDocumentRepository;
import com.rizvi.jobee.repositories.UserProfileRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class UserDocumentService {
    private final S3Service s3Service;
    private final UserDocumentRepository userDocumentRepository;
    private final UserProfileRepository userProfileRepository;

    public String uploadDocument(
            Long userId, MultipartFile document, UserDocumentType documentType) {
        try {
            var result = s3Service.uploadDocument(userId, document, documentType);
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public List<UserDocument> getUserDocuments(Long userId) {
        return userDocumentRepository.findByUserId(userId);
    }

    public UserDocument createUserDocumentViaFile(
            MultipartFile document, UserDocumentType documentType, UserProfile userProfile,
            Boolean setPrimary) {
        var userId = userProfile.getId();
        var result = uploadDocument(userId, document, documentType);
        if (result == null) {
            return null;
        }
        var userDocument = UserDocument.builder().documentType(documentType)
                .documentUrl(result).user(userProfile).build();
        userProfile.addDocument(userDocument);
        if (setPrimary) {
            userProfile.setPrimaryResume(userDocument);
            userProfileRepository.save(userProfile);
        }
        return userDocument;
    }
}
