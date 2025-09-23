package com.rizvi.jobee.helpers.AISchemas;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class PrepareForInterviewResponse {
    @JsonPropertyDescription("List of strengths that make the candidate suitable for the job")
    private List<String> strengths;
    @JsonPropertyDescription("List of weaknesses that the candidate should be aware of that could cause them to not get the job")
    private List<String> weaknesses;
    @JsonPropertyDescription("List of potential interview questions and answers")
    private List<InterviewPrepQuestion> interviewQuestions;
    @JsonPropertyDescription("List of resources for interview preparation")
    private List<InterviewPrepResource> interviewResources;
    @JsonPropertyDescription("Overall advice for the candidate")
    private String overallAdvice;
}
