package com.rizvi.Autohub.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_profiles")
public class Profile {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "bio", nullable = true)
    private String bio;

    @Column(name = "profile_picture", nullable = true)
    private String profilePictureUrl;

    @OneToOne
    @JoinColumn(name = "account_id", referencedColumnName = "id", nullable = false, unique = true)
    private Account account;

    public void addAccountToProfile(Account account) {
        this.account = account;
        account.setProfile(this);
    }
}
