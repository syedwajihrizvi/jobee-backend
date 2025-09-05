package com.rizvi.jobee.entities;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_profiles")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "title", nullable = true)
    private String title;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "summary", nullable = true)
    private String summary;

    @Column(name = "profile_image_url", nullable = true)
    private String profileImageUrl;

    @Column(name = "city", nullable = true)
    private String city;

    @Column(name = "country", nullable = true)
    private String country;

    @Column(name = "phone_number", nullable = true)
    private String phoneNumber;

    @Column(name = "company", nullable = true)
    private String company;

    @OneToOne(optional = false)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private UserAccount account;

    @OneToMany(mappedBy = "candidate", orphanRemoval = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Interview> interviews = new HashSet<>();

    @OneToMany(mappedBy = "user", orphanRemoval = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<UserDocument> documents = new HashSet<>();

    @OneToOne
    @JoinColumn(name = "primary_resume_id", unique = true)
    private UserDocument primaryResume;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "user_favorite_jobs", joinColumns = @JoinColumn(name = "user_profile_id"), inverseJoinColumns = @JoinColumn(name = "job_id"))
    private Set<Job> favoriteJobs = new HashSet<>();

    @OneToMany(mappedBy = "userProfile", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UserSkill> skills = new HashSet<>();

    @OneToMany(mappedBy = "userProfile", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Education> education = new HashSet<>();

    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Experience> experiences = new HashSet<>();

    @OneToMany(mappedBy = "userProfile", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Application> applications = new HashSet<>();

    public void setAccount(UserAccount account) {
        this.account = account;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public void toggleFavoriteJob(Job job) {
        if (job != null) {
            if (this.favoriteJobs.contains(job)) {
                this.favoriteJobs.remove(job);
            } else {
                this.favoriteJobs.add(job);
            }
        }
    }

    public void addDocument(UserDocument document) {
        if (document != null) {
            this.documents.add(document);
            document.setUser(this);
        }
    }

    public void addSkill(UserSkill userSkill) {
        if (userSkill != null) {
            this.skills.add(userSkill);
            userSkill.setUserProfile(this);
        }
    }

    public void addEducation(Education education) {
        if (education != null) {
            this.education.add(education);
            education.setUserProfile(this);
        }
    }
}