package com.rizvi.jobee.dtos.interview;

import java.util.List;

import lombok.Data;

@Data
public class InterviewPreparationDto {
    private Long id;
    private List<String> strengths;
    private List<String> weaknesses;
    private List<InterviewPrepQuestionDto> questions;
    private List<InterviewPrepResourcesDto> resources;
    private String overallAdvice;
    private List<String> notesFromInterviewer;
}
