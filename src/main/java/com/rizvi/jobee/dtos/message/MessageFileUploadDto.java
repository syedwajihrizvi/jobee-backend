package com.rizvi.jobee.dtos.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageFileUploadDto {
    private String fileUrl;
    private String fileName;
    private String fileType;
    private Long fileSize;
}
