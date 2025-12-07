package com.rizvi.jobee.principals;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CustomPrincipal {
    private Long id;
    private Long profileId;
    private String email;
    private String role;
    private String accountType;
    private Long companyId;

}
