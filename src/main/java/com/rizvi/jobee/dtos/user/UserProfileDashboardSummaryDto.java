package com.rizvi.jobee.dtos.user;

import com.rizvi.jobee.dtos.application.ApplicationDto;
import com.rizvi.jobee.dtos.company.CompanyDto;

import lombok.Data;

@Data
public class UserProfileDashboardSummaryDto {
    private Long id;
    private String fullName;
    private Integer totalApplications;
    private Integer profileViews;
    private Integer totalRejections;
    private Integer totalInConsideration;
    private Integer totalInterviews;
    private Integer totalOffers;
    private ApplicationDto lastApplication;
    private CompanyDto[] favoriteCompanies;
}
