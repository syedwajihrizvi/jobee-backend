package com.rizvi.jobee.queries;

import lombok.Data;

@Data
public class JobQuery {
    private String search;
    private String location;
    private String companyName;
    private Integer distance;
    private Integer salary;
    private Integer experience;
}
