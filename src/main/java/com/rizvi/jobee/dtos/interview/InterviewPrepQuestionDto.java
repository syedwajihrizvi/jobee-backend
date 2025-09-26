package com.rizvi.jobee.dtos.interview;

import lombok.Data;

@Data
public class InterviewPrepQuestionDto {
    private Long id;
    private String question;
    private String answer;
    private String questionAudioUrl;
    private String answerAudioUrl;
}
