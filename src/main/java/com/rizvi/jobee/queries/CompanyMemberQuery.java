package com.rizvi.jobee.queries;

import lombok.Data;

@Data
public class CompanyMemberQuery {
    private String role;
    private String search;
    private String status;
    private Long companyId;
}
