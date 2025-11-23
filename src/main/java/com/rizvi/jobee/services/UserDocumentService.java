package com.rizvi.jobee.services;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.transaction.annotation.Transactional;

import com.rizvi.jobee.entities.UserDocument;
import com.rizvi.jobee.entities.UserProfile;
import com.rizvi.jobee.enums.DocumentUrlType;
import com.rizvi.jobee.enums.UserDocumentType;
import com.rizvi.jobee.exceptions.InvalidDocumentException;
import com.rizvi.jobee.exceptions.InvalidDocumentURLLinkException;
import com.rizvi.jobee.exceptions.UserDocumentNotFoundException;
import com.rizvi.jobee.repositories.UserDocumentRepository;
import com.rizvi.jobee.repositories.UserProfileRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class UserDocumentService {
    private final S3Service s3Service;
    private final GoogleDriveService googleDriveService;
    private final DropBoxService dropboxService;
    private final OneDriveService oneDriveService;
    private final UserDocumentRepository userDocumentRepository;
    private final RequestQueue requestQueue;
    private final UserProfileRepository userProfileRepository;

    private String uploadDocument(
            Long userId, MultipartFile document, UserDocumentType documentType, String title) {
        try {
            var result = s3Service.uploadDocument(userId, document, documentType, title);
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    private String uploadDocumentImage(
            Long userId, MultipartFile documentImage, String documentType, String title) {
        try {
            var result = s3Service.uploadDocumentImage(userId, documentImage, documentType, title);
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public List<UserDocument> getUserDocuments(Long userId) {
        Sort sort = Sort.by("createdAt").descending();
        return userDocumentRepository.findByUserId(userId, sort);
    }

    public UserDocument createUserDocumentViaFile(
            MultipartFile document, UserDocumentType documentType, UserProfile userProfile,
            String title, Boolean setPrimary) {
        if (document.getSize() > 200_000) {
            throw new InvalidDocumentException("File size exceeds the maximum limit of 200KB");
        }
        var userId = userProfile.getId();
        var result = uploadDocument(userId, document, documentType, title);
        if (result == null) {
            return null;
        }
        var userDocument = UserDocument.builder().documentType(documentType)
                .documentUrl(result).title(title).user(userProfile).build();
        if (documentType == UserDocumentType.RESUME) {
            requestQueue.processResumeParsing(document, userProfile, true);
        }
        userProfile.addDocument(userDocument);
        if (setPrimary || userProfile.getPrimaryResume() == null) {
            userProfile.setPrimaryResume(userDocument);
        }
        userProfileRepository.save(userProfile);
        System.out.println("SYED-DEBUG: Document created and sending response");
        return userDocument;
    }

    public UserDocument createUserDocumentViaImage(
            MultipartFile documentImage, UserDocumentType documentType, UserProfile userProfile,
            String title) {
        if (documentImage.getSize() > 5_000_000) {
            throw new InvalidDocumentException("File size exceeds the maximum limit of 5MB");
        }
        var userId = userProfile.getId();
        var result = uploadDocumentImage(userId, documentImage, title, title);
        if (result == null) {
            return null;
        }
        var userDocument = UserDocument.builder().documentType(documentType)
                .documentUrl(result).title(title).user(userProfile).build();
        userProfile.addDocument(userDocument);
        userProfileRepository.save(userProfile);
        return userDocument;
    }

    public UserDocument userDocumentExists(Long documentId, Long userId) {
        var document = userDocumentRepository.findByIdAndUserId(documentId, userId);
        return document;
    }

    public UserDocument createDocumentViaLink(
            UserProfile userProfile, String documentLink, UserDocumentType documentType, String title,
            DocumentUrlType documentUrlType) throws InvalidDocumentURLLinkException {

        MultipartFile multipartFile = null;
        if (documentUrlType == DocumentUrlType.GOOGLE_DRIVE) {
            multipartFile = googleDriveService.createMultiPartFile(documentLink, title, documentType);
        }
        if (documentUrlType == DocumentUrlType.DROPBOX) {
            multipartFile = dropboxService.createMultiPartFile(documentLink, title, documentType);
        }
        if (documentUrlType == DocumentUrlType.ONE_DRIVE) {
            multipartFile = oneDriveService.createMultiPartFile(documentLink, title, documentType);
        }
        if (multipartFile.getSize() > 200_000) {
            throw new InvalidDocumentException("File size exceeds the maximum limit of 200KB");

        }
        if (documentType == UserDocumentType.RESUME) {
            requestQueue.processResumeParsing(multipartFile, userProfile, true);
        }
        var result = uploadDocument(userProfile.getId(), multipartFile, documentType, title);
        if (result == null) {
            return null;
        }
        var userDocument = UserDocument.builder().documentType(documentType)
                .documentUrl(result).title(title).user(userProfile).build();
        userProfile.addDocument(userDocument);
        userProfileRepository.save(userProfile);
        return userDocument;
    }

    public UserDocument updateUserDocument(
            Long documentId,
            Long userProfileId,
            String title,
            UserDocumentType documentType) {
        var userDocument = userDocumentRepository.findByIdAndUserId(documentId, userProfileId);
        if (userDocument == null) {
            throw new UserDocumentNotFoundException("Document not found for the user");
        }
        if (title != null && !title.isBlank()) {
            userDocument.setTitle(title);
        }
        if (documentType != null) {
            userDocument.setDocumentType(documentType);
        }
        userDocumentRepository.save(userDocument);
        return userDocument;
    }

    @Transactional
    public void deleteUserDocument(Long documentId, Long userId) {
        var userDocument = userDocumentRepository.findByIdAndUserId(documentId, userId);
        if (userDocument == null) {
            System.out.println("SYED-DEBUG: Document not found for deletion: " + documentId);
            throw new UserDocumentNotFoundException("Document not found for the user");
        }

        var userProfile = userDocument.getUser();
        userProfile.getDocuments().remove(userDocument);
        if (userProfile.getPrimaryResume() != null &&
                userProfile.getPrimaryResume().getId().equals(documentId)) {
            userProfile.setPrimaryResume(null);
            System.out.println("SYED-DEBUG: Removed primary resume reference for user ID: " + userProfile.getId());
        }
        userProfileRepository.save(userProfile);
        userDocumentRepository.delete(userDocument);
    }
}
