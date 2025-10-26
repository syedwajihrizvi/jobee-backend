package com.rizvi.jobee.interfaces;

import org.springframework.web.multipart.MultipartFile;

import com.rizvi.jobee.enums.UserDocumentType;

public interface LinkService {
    String convertShareUrlToDownloadUrl(String shareUrl);

    MultipartFile createMultiPartFile(String shareUrl, String title, UserDocumentType documentType);
}
