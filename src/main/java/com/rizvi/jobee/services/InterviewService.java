package com.rizvi.jobee.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rizvi.jobee.dtos.interview.ConductorDto;
import com.rizvi.jobee.dtos.interview.CreateInterviewDto;
import com.rizvi.jobee.entities.Application;
import com.rizvi.jobee.entities.BusinessAccount;
import com.rizvi.jobee.entities.Interview;
import com.rizvi.jobee.entities.InterviewPreparation;
import com.rizvi.jobee.entities.Job;
import com.rizvi.jobee.entities.UserProfile;
import com.rizvi.jobee.enums.ApplicationStatus;
import com.rizvi.jobee.enums.InterviewStatus;
import com.rizvi.jobee.enums.PreparationStatus;
import com.rizvi.jobee.exceptions.InterviewNotFoundException;
import com.rizvi.jobee.helpers.AISchemas.AICandidate;
import com.rizvi.jobee.helpers.AISchemas.AICompany;
import com.rizvi.jobee.helpers.AISchemas.AIInterview;
import com.rizvi.jobee.helpers.AISchemas.AIJob;
import com.rizvi.jobee.helpers.AISchemas.PrepareForInterviewRequest;
import com.rizvi.jobee.repositories.ApplicationRepository;
import com.rizvi.jobee.repositories.BusinessAccountRepository;
import com.rizvi.jobee.repositories.InterviewPreparationRepository;
import com.rizvi.jobee.repositories.InterviewRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class InterviewService {
    private final BusinessAccountRepository businessAccountRepository;
    private final InterviewRepository interviewRepository;
    private final InterviewPreparationRepository interviewPreparationRepository;
    private final ApplicationRepository applicationRepository;
    private final InterviewPrepQueue interviewPrepQueue;

    public List<Interview> getAllInterviews() {
        return interviewRepository.findAll();
    }

    public Interview getInterviewById(Long id) {
        return interviewRepository.findById(id)
                .orElseThrow(() -> new InterviewNotFoundException("Interview not found with id: " + id));
    }

    public Interview getInterviewByJobIdAndCandidateId(Long jobId, Long candidateId) {
        return interviewRepository.findByJobIdAndCandidateId(jobId, candidateId);
    }

    public List<Interview> getInterviewsByJobId(Long jobId, Number limit) {
        if (limit != null) {
            return interviewRepository.findByJobIdWithLimit(jobId, limit);
        }
        return interviewRepository.findByJobId(jobId);
    }

    public List<Interview> getInterviewsByCandidate(Long candidateId) {
        return interviewRepository.findByCandidateId(candidateId);
    }

    @Transactional
    public Interview createInterview(
            CreateInterviewDto request, BusinessAccount businessAccount,
            UserProfile candidate, Job job, Application application) {
        var interview = Interview.builder()
                .job(job)
                .candidate(candidate)
                .title(request.getTitle())
                .description(request.getDescription())
                .interviewDate(request.getInterviewDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .interviewType(request.getInterviewType())
                .timezone(request.getTimezone())
                .location(request.getLocation())
                .meetingLink(request.getMeetingLink())
                .phoneNumber(request.getPhoneNumber())
                .status(InterviewStatus.SCHEDULED)
                .createdBy(businessAccount)
                .build();
        for (ConductorDto conductor : request.getConductors()) {
            var interviewer = businessAccountRepository.findByEmail(conductor.getEmail()).orElse(null);
            if (interviewer == null) {
                interview.addOtherInterviewer(conductor);
            } else {
                interview.addInterviewer(interviewer);
            }
        }
        var savedInterview = interviewRepository.save(interview);
        application.setStatus(ApplicationStatus.INTERVIEW_SCHEDULED);
        applicationRepository.save(application);
        return savedInterview;
    }

    @Transactional
    public Boolean prepareForInterview(Long interviewId, Long candidateId) {
        // If the interview already has a preparation, return False since we cannot
        // reprepare again
        var interview = interviewRepository.findInterviewForPreparation(interviewId);
        if (interview == null) {
            throw new InterviewNotFoundException("Interview not found with id: " + interviewId);
        }
        if (!interview.getCandidate().getId().equals(candidateId)) {
            return false;
        }
        // Create new interview preparation
        var interviewPreparation = InterviewPreparation.builder()
                .interview(interview)
                .status(PreparationStatus.IN_PROGRESS)
                .build();
        var savedInterviewPreparation = interviewPreparationRepository.save(interviewPreparation);
        AIJob aiJob = new AIJob(interview.getJob());
        AICompany aiCompany = new AICompany(interview.getJob().getBusinessAccount().getCompany());
        AICandidate aiCandidate = new AICandidate(interview.getCandidate());
        AIInterview aiInterview = new AIInterview(interview);

        PrepareForInterviewRequest prepareForInterviewRequest = new PrepareForInterviewRequest(aiJob, aiCompany,
                aiCandidate, aiInterview);
        interviewPrepQueue.processInterviewPrep(prepareForInterviewRequest, savedInterviewPreparation);

        return true;
    }
}
