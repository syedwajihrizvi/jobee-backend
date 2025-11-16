package com.rizvi.jobee.entities;

import java.io.Serializable;
import java.util.Objects;

public class UserFavoriteJobId implements Serializable {

    private Long userProfile; // store the FK value
    private Long job;

    public UserFavoriteJobId() {
    }

    public UserFavoriteJobId(Long userProfile, Long job) {
        this.userProfile = userProfile;
        this.job = job;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UserFavoriteJobId))
            return false;
        UserFavoriteJobId that = (UserFavoriteJobId) o;
        return Objects.equals(userProfile, that.userProfile) &&
                Objects.equals(job, that.job);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userProfile, job);
    }
}
