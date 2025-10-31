package com.rizvi.jobee.services;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rizvi.jobee.enums.UserDocumentType;
import com.rizvi.jobee.exceptions.InvalidDocumentException;
import com.rizvi.jobee.exceptions.InvalidDocumentURLLinkException;
import com.rizvi.jobee.interfaces.LinkService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class DropBoxService implements LinkService {
    private final FileService fileService;

    public String convertShareUrlToDownloadUrl(String shareUrl) {
        if (shareUrl == null || shareUrl.isEmpty() || !shareUrl.contains("dropbox.com")) {
            throw new InvalidDocumentURLLinkException("Invalid Dropbox URL");
        }
        String downloadExtension = "dl=1";
        shareUrl = shareUrl.replace("dl=0", downloadExtension);
        return shareUrl;
    }

    public MultipartFile createMultiPartFile(String shareUrl, String title, UserDocumentType documentType) {
        String downloadUrl = convertShareUrlToDownloadUrl(shareUrl);
        System.out.println("Dropbox download URL: " + downloadUrl);
        URI uri = URI.create(downloadUrl);
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
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("GET");
            connection.connect();
            String contentType = null;
            byte[] documentBytes = inputStream.readAllBytes();
            contentType = fileService.detectMimeType(url, documentBytes, title);
            var documentTitle = title != null && !title.isEmpty() ? title
                    : "dropbox_document_" + documentType.name().toLowerCase();
            System.out.println("Formatted title of documet: " + documentTitle);
            MultipartFile multipartFile = new MockMultipartFile(documentTitle, documentTitle,
                    contentType, documentBytes);
            if (multipartFile.getSize() > 10_000_000) {
                throw new InvalidDocumentException("File size exceeds the maximum limit of 200KB");

            }
            return multipartFile;
        } catch (Exception e) {
            System.out.println("Exception while creating MultipartFile from Dropbox URL: " + e.getMessage());
            return null;
        }
    }
}
