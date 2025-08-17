package com.rizvi.jobee.services;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rizvi.jobee.config.AWSProperties;
import com.rizvi.jobee.enums.UserDocumentType;

import lombok.AllArgsConstructor;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@AllArgsConstructor
@Service
public class S3Service {
        private S3Client s3Client;

        private AWSProperties awsProperties;

        public void uploadDocument(
                        Long userId, MultipartFile document, UserDocumentType documentType) throws IOException {
                final String key = "user-documents/" + documentType + "/" + userId + "/"
                                + document.getOriginalFilename();
                System.out.println("Uploading document to S3 with key: " + key);
                s3Client.putObject(
                                PutObjectRequest.builder()
                                                .bucket(awsProperties.getBucket())
                                                .key(key).contentType(document.getContentType())
                                                .build(),
                                software.amazon.awssdk.core.sync.RequestBody.fromInputStream(document.getInputStream(),
                                                document.getSize()));
        }

        public void uploadProfileImage(Long userId, MultipartFile profileImage) throws IOException {
                final String key = "user-profile-images/" + userId + "_" + profileImage.getOriginalFilename();
                System.out.println("Uploading profile image to S3 with key: " + key);
                s3Client.putObject(
                                PutObjectRequest.builder()
                                                .bucket(awsProperties.getBucket())
                                                .key(key).contentType(profileImage.getContentType())
                                                .build(),
                                software.amazon.awssdk.core.sync.RequestBody.fromInputStream(
                                                profileImage.getInputStream(),
                                                profileImage.getSize()));
        }
}
