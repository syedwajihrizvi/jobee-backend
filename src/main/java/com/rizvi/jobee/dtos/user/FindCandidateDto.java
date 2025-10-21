package com.rizvi.jobee.dtos.user;

import lombok.Data;

@Data
public class FindCandidateDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String profileImageUrl;
    private String title;
    private Integer matchScore;
    private String location;
}
