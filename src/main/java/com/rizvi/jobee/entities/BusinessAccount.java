package com.rizvi.jobee.entities;

import java.util.HashSet;
import java.util.Set;

import com.rizvi.jobee.enums.BusinessType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "business_accounts")
public class BusinessAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private BusinessType accountType;;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Builder.Default
    @ManyToMany(mappedBy = "interviewers", fetch = jakarta.persistence.FetchType.LAZY)
    private Set<Interview> interviews = new HashSet<>();

    @OneToMany(mappedBy = "createdBy", orphanRemoval = true, cascade = CascadeType.ALL)
    public Set<Interview> createdInterviews;

    @OneToMany(mappedBy = "businessAccount", orphanRemoval = true, cascade = CascadeType.ALL)
    public Set<Job> jobs;

    public void setCompany(Company company) {
        this.company = company;
        company.getBusinessAccounts().add(this);
    }

    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }
}
