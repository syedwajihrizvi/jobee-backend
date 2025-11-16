package com.rizvi.jobee.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.rizvi.jobee.dtos.job.JobSummaryDto;
import com.rizvi.jobee.dtos.job.PaginatedResponse;
import com.rizvi.jobee.entities.UserFavoriteJob;
import com.rizvi.jobee.mappers.JobMapper;
import com.rizvi.jobee.repositories.UserFavoriteJobRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserFavoriteJobService {

    private final UserFavoriteJobRepository userFavoriteJobRepository;
    private final JobMapper jobMapper;

    public PaginatedResponse<JobSummaryDto> getFavoriteJobs(Long userProfileId, int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("favoritedAt").descending());
        Page<UserFavoriteJob> page = userFavoriteJobRepository.findByUserProfileId(userProfileId, pageable);

        var favoriteJobs = page.getContent().stream().map(favoriteJob -> favoriteJob.getJob());
        var dtos = favoriteJobs.map(jobMapper::toSummaryDto).toList();
        var hasMore = pageNumber < page.getTotalPages() - 1;
        return new PaginatedResponse<>(hasMore, dtos, page.getTotalElements());
    }

}