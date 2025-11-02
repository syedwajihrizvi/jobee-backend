package com.rizvi.jobee.queries;

import java.util.List;

import lombok.Data;

@Data
public class JobQuery {
    private String search;
    private List<String> locations;
    private List<String> companies;
    private List<String> tags;
    private Integer distance;
    private Integer minSalary;
    private Integer maxSalary;
    private Long companyId;
    private List<String> employmentTypes;
    private List<String> experience;
    private List<String> settings;
}
