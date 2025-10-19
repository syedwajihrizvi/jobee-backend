package com.rizvi.jobee.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.rizvi.jobee.dtos.job.CreateJobDto;
import com.rizvi.jobee.dtos.job.PaginatedJobDto;
import com.rizvi.jobee.entities.BusinessAccount;
import com.rizvi.jobee.entities.Job;
import com.rizvi.jobee.entities.Tag;
import com.rizvi.jobee.exceptions.JobNotFoundException;
import com.rizvi.jobee.queries.JobQuery;
import com.rizvi.jobee.repositories.JobRepository;
import com.rizvi.jobee.repositories.TagRepository;
import com.rizvi.jobee.specifications.JobSpecifications;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class JobService {
    private final JobRepository jobRepository;
    private final TagRepository tagRepository;

    public PaginatedJobDto getAllJobs(JobQuery jobQuery, int pageNumber, int pageSize) {
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        Page<Job> page = jobRepository.findAll(JobSpecifications.withFilters(jobQuery), pageRequest);
        var jobs = page.getContent();
        var hasMore = pageNumber < page.getTotalPages() - 1;
        return new PaginatedJobDto(hasMore, jobs);
    }

    public Job getJobById(Long jobId) {
        var job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            throw new JobNotFoundException("Job with id " + jobId + " not found");
        }
        return job;
    }

    public PaginatedJobDto getJobsByCompany(JobQuery jobQuery, Long companyId, int pageNumber, int pageSize) {
        jobQuery.setCompanyId(companyId);
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        Page<Job> page = jobRepository.findAll(JobSpecifications.withFilters(jobQuery), pageRequest);
        var jobs = page.getContent();
        var hasMore = pageNumber < page.getTotalPages() - 1;
        var totalElements = page.getTotalElements();
        return new PaginatedJobDto(hasMore, jobs, totalElements);
    }

    public Job getCompanyJobById(Long jobId) {
        var job = jobRepository.findDetailedJobById(jobId).orElse(null);
        if (job == null) {
            throw new JobNotFoundException("Job with id " + jobId + " not found");
        }
        return job;
    }

    public List<Job> getJobsByIds(List<Long> jobIds) {
        return jobRepository.findJobWithIdList(jobIds);
    }

    @Transactional
    public Job createJob(CreateJobDto request, BusinessAccount businessAccount) {
        var tagEntities = new ArrayList<Tag>();
        for (String tagName : request.getTags()) {
            var tag = tagRepository.findByName(tagName.trim().replaceAll("[^a-zA-Z0-9 ]", ""));
            if (tag == null) {
                tag = Tag.builder().name(tagName).build();
                tag = tagRepository.save(tag);
            }
            tagEntities.add(tag);
        }
        var job = Job.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .employmentType(request.getEmploymentType())
                .setting(request.getSetting())
                .appDeadline(request.getAppDeadline())
                .minSalary(request.getMinSalary())
                .maxSalary(request.getMaxSalary())
                .experience(request.getExperience())
                .build();
        job.setBusinessAccount(businessAccount);
        for (Tag tag : tagEntities) {
            job.addTag(tag);
        }
        var savedJob = jobRepository.save(job);
        return savedJob;

    }

    public void incrementJobViews(Long jobId) {
        var job = jobRepository.findById(jobId).orElseThrow(() -> new JobNotFoundException("Job not found"));
        job.setViews(job.getViews() + 1);
        jobRepository.save(job);
    }

    public List<Job> getJobsByBusinessAccountId(Long accountId, String search) {
        if (search != null && !search.isEmpty()) {
            return jobRepository.findByBusinessAccountIdAndTitle(accountId, search);
        }
        return jobRepository.findByBusinessAccountId(accountId);
    }

    public List<Job> getJobsByCompanyId(Long companyId) {
        return jobRepository.findByCompanyId(companyId);
    }

    public List<Job> getMostRecentJobsByCompany(Long companyId, Long limit) {
        var jobs = jobRepository.findMostRecentJobsByCompanyId(companyId);
        return jobs.size() > limit ? jobs.subList(0, limit.intValue()) : jobs;
    }
}
