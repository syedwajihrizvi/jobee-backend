package com.rizvi.jobee.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rizvi.jobee.dtos.interview.ConductorDto;
import com.rizvi.jobee.dtos.interview.CreateInterviewDto;
import com.rizvi.jobee.entities.Application;
import com.rizvi.jobee.entities.BusinessAccount;
import com.rizvi.jobee.entities.Interview;
import com.rizvi.jobee.entities.Job;
import com.rizvi.jobee.entities.UserProfile;
import com.rizvi.jobee.enums.ApplicationStatus;
import com.rizvi.jobee.enums.InterviewStatus;
import com.rizvi.jobee.exceptions.InterviewNotFoundException;
import com.rizvi.jobee.repositories.ApplicationRepository;
import com.rizvi.jobee.repositories.BusinessAccountRepository;
import com.rizvi.jobee.repositories.InterviewRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class InterviewService {
    private final BusinessAccountRepository businessAccountRepository;
    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;

    public List<Interview> getAllInterviews() {
        return interviewRepository.findAll();
    }

    public Interview getInterviewById(Long id) {
        return interviewRepository.findById(id)
                .orElseThrow(() -> new InterviewNotFoundException("Interview not found with id: " + id));
    }

    public List<Interview> getInterviewsByCandidate(Long candidateId) {
        return interviewRepository.findByCandidateId(candidateId);
    }

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
}
