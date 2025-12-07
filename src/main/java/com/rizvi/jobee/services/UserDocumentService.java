package com.rizvi.jobee.services;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
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
    private final FileService fileService;

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
        // Convert documen to pdf if it is not pdf so S3 Previews it better
        var finalDocument = document;
        if (!document.getContentType().equals("application/pdf")) {
            try {
                finalDocument = fileService.convertBytesToMultipartFile(
                        document.getBytes(), document.getOriginalFilename(), "application/pdf");
            } catch (Exception e) {
                System.out.println("SYED-DEBUG: Error converting document to PDF: " + e.getMessage());
            }
        }
        var result = uploadDocument(userId, finalDocument, documentType, title);
        if (result == null) {
            return null;
        }
        var userDocument = UserDocument.builder().documentType(documentType)
                .documentUrl(result).title(title).user(userProfile).build();
        var savedDocument = userDocumentRepository.save(userDocument);
        byte[] previewBytes;
        try {
            previewBytes = renderPdfPreview(document);
            String previewImageUrl = s3Service.uploadDocumentPreviewImage(savedDocument.getId(), previewBytes);
            savedDocument.setPreviewUrl(previewImageUrl);
        } catch (Exception e) {
            System.out.println("SYED-DEBUG: Error generating PDF preview: " + e.getMessage());
        }
        if (documentType == UserDocumentType.RESUME) {
            try {
                requestQueue.processResumeParsing(document, userProfile, true);
                System.out.println("SYED-DEBUG: Async resume parsing queued for user ID: " + userId);
            } catch (Exception e) {
                System.out.println("SYED-DEBUG: Failed to queue resume parsing: " + e.getMessage());
            }
        }
        userProfile.addDocument(savedDocument);
        if (setPrimary || userProfile.getPrimaryResume() == null) {
            userProfile.setPrimaryResume(savedDocument);
        }
        userProfileRepository.save(userProfile);
        return savedDocument;
    }

    public UserDocument createUserDocumentViaImage(
            MultipartFile documentImage, UserDocumentType documentType, UserProfile userProfile,
            String title) {
        if (documentImage.getSize() > 5_000_000) {
            throw new InvalidDocumentException("File size exceeds the maximum limit of 5MB");
        }
        var userId = userProfile.getId();
        var result = uploadDocumentImage(userId, documentImage, documentType.toString(), title);
        if (result == null) {
            return null;
        }
        var userDocument = UserDocument.builder().documentType(documentType)
                .documentUrl(result).title(title).formatType("IMG").user(userProfile).build();
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

    private byte[] renderPdfPreview(MultipartFile document) throws IOException {
        System.out.println("SYED-DEBUG: Rendering PDF preview for document: " + document.getContentType());
        // If it is not pdf type, we convert to pdf
        byte[] pdfBytes = new byte[0];
        if (!document.getContentType().equals("application/pdf")) {
            System.out.println("SYED-DEBUG: Document is not PDF, converting to PDF from: " + document.getContentType());
            try {
                pdfBytes = fileService.convertDocxToPdf(document.getBytes());
            } catch (Exception e) {
                System.out.println("SYED-DEBUG: Error converting document to PDF: " + e.getMessage());
                return pdfBytes;
            }
            System.out.println("SYED-DEBUG: Converting document to PDF for preview generation.");
        } else {
            pdfBytes = document.getBytes();
        }
        try (PDDocument pdfDocument = PDDocument.load(pdfBytes)) {
            PDFRenderer renderer = new PDFRenderer(pdfDocument);
            BufferedImage image = renderer.renderImageWithDPI(0, 120);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        }
    }

}
