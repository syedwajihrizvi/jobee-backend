package com.rizvi.jobee.services;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;

import com.rizvi.jobee.enums.UserDocumentType;
import com.rizvi.jobee.exceptions.InvalidDocumentException;
import com.rizvi.jobee.interfaces.LinkService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class OneDriveService implements LinkService {
    private final FileService fileService;
    private final RestTemplate restTemplate;

    public String convertShareUrlToDownloadUrl(String shareUrl) {
        if (shareUrl == null || shareUrl.isEmpty() || !shareUrl.contains("1drv.ms")) {
            throw new IllegalArgumentException("Invalid OneDrive URL");
        }
        String downloadUrl = null;
        if (shareUrl.contains("1drv.ms")) {
            var response = restTemplate.exchange(shareUrl, HttpMethod.GET, null, String.class);
            var redirectUrl = response.getHeaders().getLocation();
            if (redirectUrl != null) {
                downloadUrl = redirectUrl.toString();
            }
        }
        if (shareUrl.contains("onedrive.live.com")) {
            downloadUrl = shareUrl.replace("redir?", "download?");
        }
        return downloadUrl;
    }

    public MultipartFile createMultiPartFile(String shareUrl, String title, UserDocumentType documentType) {
        URI uri = URI.create(shareUrl);
        URL url = null;
        try {
            url = uri.toURL();
        } catch (MalformedURLException e) {
            System.out.println("MalformedURLException: " + e.getMessage());
            return null;
        }
        InputStream inputStream = null;
        try {
            inputStream = url.openStream();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.connect();
            byte[] documentBytes = inputStream.readAllBytes();
            String contentType = fileService.detectMimeType(url, documentBytes, title);
            System.out.println("Content-Type of the document: " + contentType);
            System.out.println("Successfully read " + documentBytes.length + " bytes from One Drive document.");
            var documentTitle = title != null ? title : "one_drive_document_" + documentType.name().toLowerCase();
            MultipartFile multipartFile = new MockMultipartFile(documentTitle, documentTitle,
                    contentType, documentBytes);
            if (multipartFile.getSize() > 5_000_000) {
                throw new InvalidDocumentException("File size exceeds the maximum limit of 200KB");

            }
            return multipartFile;
        } catch (Exception e) {
            System.out.println("Exception while creating MultipartFile from One Drive URL: " + e.getMessage());
            return null;
        }
    }

}
