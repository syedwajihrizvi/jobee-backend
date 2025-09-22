package com.rizvi.jobee.entities;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "companies")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "website", nullable = true)
    private String website;

    @Column(name = "hq_city", nullable = true)
    private String hqCity;

    @Column(name = "hq_state", nullable = true)
    private String hqState;

    @Column(name = "hq_country", nullable = true)
    private String hqCountry;

    @Column(name = "founded_year", nullable = true)
    private Integer foundedYear;

    @Column(name = "num_employees", nullable = true)
    private Integer numEmployees;

    @Column(name = "industry", nullable = true)
    private String industry;

    @Column(name = "description", nullable = true)
    private String description;

    @OneToMany(mappedBy = "company", orphanRemoval = true, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<BusinessAccount> businessAccounts = new HashSet<>();

    public void addBusinessAccount(BusinessAccount account) {
        businessAccounts.add(account);
        account.setCompany(this);
    }

}
