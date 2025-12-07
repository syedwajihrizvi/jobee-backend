package com.rizvi.jobee.dtos.interview;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.rizvi.jobee.enums.InterviewType;
import com.rizvi.jobee.enums.Timezone;

import lombok.Data;

@Data
public class CreateInterviewDto {
    private String title;
    private String description;
    private Long jobId;
    private Long candidateId;
    private Long applicationId;
    private LocalDate interviewDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private InterviewType interviewType;
    private Timezone timezone;
    private String streetAddress;
    private String buildingName;
    private String parkingInfo;
    private String contactInstructionsOnArrival;
    private String meetingLink;
    private String meetingPlatform;
    private String phoneNumber;
    private Long previousInterviewId;
    private List<ConductorDto> conductors;
    private List<String> preparationTipsFromInterviewer;
    private ZoomMeetingDetailsDto zoomMeetingDetails;
    private GoogleMeetingDetailsDto googleMeetingDetails;
}
