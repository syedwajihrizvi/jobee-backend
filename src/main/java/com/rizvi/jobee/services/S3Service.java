package com.rizvi.jobee.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rizvi.jobee.config.AWSProperties;
import com.rizvi.jobee.enums.UserDocumentType;

import lombok.AllArgsConstructor;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;

@AllArgsConstructor
@Service
public class S3Service {
        private S3Client s3Client;
        private AWSProperties awsProperties;

        public static String generateMessageAttachmentUrl(String fileUrl) {
                return "https://" + "your-bucket-name" + ".s3.amazonaws.com/message-files/" + fileUrl;
        }

        public String uploadDocument(
                        Long userId,
                        MultipartFile document,
                        UserDocumentType documentType,
                        String title)
                        throws IOException {
                String originalName = title;
                String safeFileName = originalName
                                .trim()
                                .replaceAll("\\s+", "_") // replace spaces with underscores
                                .replaceAll("[^a-zA-Z0-9._-]", ""); // allow only safe characters
                String contentType = document.getContentType();
                String fileExtension = "";
                if (!contentType.equals("application/pdf")) {
                        fileExtension = "docx";
                } else {
                        fileExtension = "pdf";
                }
                final String key = "user-documents/" + documentType + "/" + userId + "/"
                                + safeFileName + "." + fileExtension;
                // Convert to pdf if needed

                s3Client.putObject(
                                PutObjectRequest.builder()
                                                .bucket(awsProperties.getBucket())
                                                .key(key)
                                                .contentType(contentType)
                                                .build(),
                                RequestBody.fromInputStream(document.getInputStream(),
                                                document.getSize()));
                return documentType + "/" + userId + "/" + safeFileName + "." + fileExtension;
        }

        public void uploadProfileImage(Long userId, MultipartFile profileImage) throws IOException {
                final String key = "user-profile-images/" + userId + "_" + profileImage.getOriginalFilename();
                s3Client.putObject(
                                PutObjectRequest.builder()
                                                .bucket(awsProperties.getBucket())
                                                .key(key).contentType(profileImage.getContentType())
                                                .build(),
                                software.amazon.awssdk.core.sync.RequestBody.fromInputStream(
                                                profileImage.getInputStream(),
                                                profileImage.getSize()));
        }

        public void uploadCompanyLogo(Long companyId, MultipartFile logoImage) throws IOException {
                final String key = "company-logos/" + companyId;
                s3Client.putObject(
                                PutObjectRequest.builder()
                                                .bucket(awsProperties.getBucket())
                                                .key(key).contentType(logoImage.getContentType())
                                                .build(),
                                software.amazon.awssdk.core.sync.RequestBody.fromInputStream(
                                                logoImage.getInputStream(),
                                                logoImage.getSize()));
        }

        public String uploadDocumentPreviewImage(
                        Long documentId,
                        byte[] imageBytes) {
                final String key = "document-previews/" + documentId + ".png";
                s3Client.putObject(
                                PutObjectRequest.builder()
                                                .bucket(awsProperties.getBucket())
                                                .key(key).contentType("image/png")
                                                .build(),
                                software.amazon.awssdk.core.sync.RequestBody.fromBytes(imageBytes));
                return "document-previews/" + documentId + ".png";
        }

        public String uploadDocumentImage(
                        Long userId,
                        MultipartFile documentImage,
                        String documentType,
                        String title) throws IOException {
                final String key = "user-documents/" + documentType + "/" + userId + "/" +
                                documentImage.getOriginalFilename();
                s3Client.putObject(
                                PutObjectRequest.builder()
                                                .bucket(awsProperties.getBucket())
                                                .key(key).contentType(documentImage.getContentType())
                                                .build(),
                                software.amazon.awssdk.core.sync.RequestBody.fromInputStream(
                                                documentImage.getInputStream(),
                                                documentImage.getSize()));
                return documentType + "/" + userId + "/" + documentImage.getOriginalFilename();
        }

        public void updateBusinessProfileImage(Long userId, MultipartFile profileImage) throws IOException {
                final String key = "business-profile-images/" + userId + "_" + profileImage.getOriginalFilename();
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

        public String detectMimeType(URL url, byte[] contentBytes, String originalFileName) {
                try {
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("HEAD");
                        connection.setConnectTimeout(10000);
                        connection.setReadTimeout(10000);
                        connection.connect();
                        String mimeType = connection.getContentType();
                        System.out.println("Detected MIME type from HTTP headers: " + mimeType);
                        if (mimeType != null && !mimeType.isEmpty() && !mimeType.equals("application/octet-stream")) {
                                return mimeType;
                        }

                        String contentDisposition = connection.getHeaderField("Content-Disposition");
                        System.out.println("Content-Disposition header: " + contentDisposition);
                        if (contentDisposition != null) {
                                // pattern: filename="something.pdf" or filename=some.pdf
                                Pattern p = Pattern.compile("filename\\*=UTF-8''(.+)|filename=\"?([^\";]+)\"?");
                                Matcher m = p.matcher(contentDisposition);
                                if (m.find()) {
                                        String filename = m.group(1) != null ? m.group(1) : m.group(2);
                                        if (filename != null && filename.contains(".")) {
                                                String ext = filename.substring(filename.lastIndexOf("."));
                                                String probe = probeMimeFromExtension(ext);
                                                if (probe != null)
                                                        return probe;
                                        }
                                }
                        }
                } catch (IOException e) {
                        // TODO: handle exception
                }

                // Fallback: detect from original file name extension
                if (originalFileName != null && originalFileName.contains(".")) {
                        String ext = originalFileName.substring(originalFileName.lastIndexOf("."));
                        String probe = probeMimeFromExtension(ext);
                        if (probe != null)
                                return probe;
                }

                try {
                        Path temp = Files.createTempFile("jobee_mime_detect_", null);
                        Files.write(temp, contentBytes);
                        String probe = Files.probeContentType(temp);
                        Files.deleteIfExists(temp);
                        System.out.println("Detected MIME type from file probe: " + probe);
                        if (probe != null && !probe.equals("application/octet-stream")) {
                                return probe;
                        }
                } catch (Exception e) {
                        System.out.println("Exception during MIME type probing: " + e.getMessage());
                }

                try {
                        Tika tika = new Tika();
                        String mimeType = tika.detect(contentBytes);
                        System.out.println("Detected MIME type from Apache Tika: " + mimeType);
                        if (mimeType != null && !mimeType.equals("application/octet-stream")) {
                                return mimeType;
                        }
                } catch (Throwable ignored) {
                }
                return "application/octet-stream";
        }

        public String probeMimeFromExtension(String ext) {
                if (ext == null)
                        return null;
                ext = ext.toLowerCase();
                switch (ext) {
                        case ".pdf":
                                return "application/pdf";
                        case ".doc":
                                return "application/msword";
                        case ".docx":
                                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                        case ".txt":
                                return "text/plain";
                        case ".rtf":
                                return "application/rtf";
                        case ".odt":
                                return "application/vnd.oasis.opendocument.text";
                        case ".jpg":
                        case ".jpeg":
                                return "image/jpeg";
                        case ".png":
                                return "image/png";
                        default:
                                return null;
                }
        }

        public String uploadMessageFile(MultipartFile file, Long userId, String fileType) throws IOException {
                String originalName = file.getOriginalFilename();
                String safeFileName = originalName
                                .trim()
                                .replaceAll("\\s+", "_")
                                .replaceAll("[^a-zA-Z0-9._-]", "");

                String timestamp = String.valueOf(System.currentTimeMillis());
                final String key = "message-files/" + fileType + "/" + userId + "/" + timestamp + "_" + safeFileName;

                s3Client.putObject(
                                PutObjectRequest.builder()
                                                .bucket(awsProperties.getBucket())
                                                .key(key)
                                                .contentType(file.getContentType())
                                                .build(),
                                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

                return fileType + "/" + userId + "/" + timestamp + "_" + safeFileName;
        }

        public File getS3File(String fileUrl, String requestType) {
                String key;
                if (requestType.equals("MESSAGE_FILE")) {
                        key = "message-files/" + fileUrl;
                } else {
                        key = "user-documents/" + fileUrl;
                }
                GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                                .bucket(awsProperties.getBucket())
                                .key(key)
                                .build();
                File localFile = new File("/tmp/" + Path.of(key).getFileName().toString());
                try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest)) {
                        FileOutputStream fos = new FileOutputStream(localFile);
                        byte[] readBuf = new byte[1024];
                        int readLen = 0;
                        while ((readLen = s3Object.read(readBuf)) > 0) {
                                fos.write(readBuf, 0, readLen);
                        }
                        fos.close();
                } catch (IOException e) {
                        System.out.println("Exception while getting S3 file: " + e.getMessage());
                } finally {
                        s3Client.close();
                }
                return localFile;
        }

}
