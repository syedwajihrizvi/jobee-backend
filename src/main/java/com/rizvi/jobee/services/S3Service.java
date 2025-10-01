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

        public String uploadDocument(
                        Long userId, MultipartFile document, UserDocumentType documentType) throws IOException {
                String originalName = document.getOriginalFilename();
                System.out.println("Original file name: " + originalName);
                String safeFileName = originalName
                                .trim()
                                .replaceAll("\\s+", "_") // replace spaces with underscores
                                .replaceAll("[^a-zA-Z0-9._-]", ""); // allow only safe characters
                final String key = "user-documents/" + documentType + "/" + userId + "/"
                                + safeFileName;
                s3Client.putObject(
                                PutObjectRequest.builder()
                                                .bucket(awsProperties.getBucket())
                                                .key(key).contentType(document.getContentType())
                                                .build(),
                                software.amazon.awssdk.core.sync.RequestBody.fromInputStream(document.getInputStream(),
                                                document.getSize()));
                return documentType + "/" + userId + "/" + document.getOriginalFilename();
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

        public void uploadVideoIntro(Long userId, MultipartFile videoIntro) throws IOException {
                final String key = "user-video-intros/" + userId;
                System.out.println("Uploading video intro to S3 with key: " + key);
                s3Client.putObject(
                                PutObjectRequest.builder()
                                                .bucket(awsProperties.getBucket())
                                                .key(key).contentType(videoIntro.getContentType())
                                                .build(),
                                software.amazon.awssdk.core.sync.RequestBody.fromInputStream(
                                                videoIntro.getInputStream(),
                                                videoIntro.getSize()));
        }

        public void deleteFile(String key) throws IOException {
                s3Client.deleteObject(builder -> builder.bucket(awsProperties.getBucket()).key(key).build());
        }

        public String uploadInterviewPrepQuestionAudio(Long interviewId, Long questionId, byte[] audioData)
                        throws IOException {
                final String key = "interview-prep/" + interviewId + "/" + questionId + "-question" + ".mp3";
                s3Client.putObject(
                                PutObjectRequest.builder()
                                                .bucket(awsProperties.getBucket())
                                                .key(key).contentType("audio/mpeg")
                                                .build(),
                                software.amazon.awssdk.core.sync.RequestBody.fromBytes(audioData));
                return questionId + "-question" + ".mp3";

        }

        public String uploadInterviewPrepQuestionAnswerAudio(Long interviewId, Long questionId, byte[] audioData)
                        throws IOException {
                final String key = "interview-prep/" + interviewId + "/" + questionId + "-answer" + ".mp3";
                s3Client.putObject(
                                PutObjectRequest.builder()
                                                .bucket(awsProperties.getBucket())
                                                .key(key).contentType("audio/mpeg")
                                                .build(),
                                software.amazon.awssdk.core.sync.RequestBody.fromBytes(audioData));
                return questionId + "-answer.mp3";
        }

        public String uploadInterviewPrepQuestionAIAnswerAudio(Long interviewId, Long questionId, byte[] audioData)
                        throws IOException {
                final String key = "interview-prep/" + interviewId + "/" + questionId + "-ai-answer" + ".mp3";
                s3Client.putObject(
                                PutObjectRequest.builder()
                                                .bucket(awsProperties.getBucket())
                                                .key(key).contentType("audio/mpeg")
                                                .build(),
                                software.amazon.awssdk.core.sync.RequestBody.fromBytes(audioData));
                return questionId + "-ai-answer.mp3";
        }
}
