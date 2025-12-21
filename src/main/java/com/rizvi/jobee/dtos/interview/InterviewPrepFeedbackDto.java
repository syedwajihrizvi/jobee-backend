package com.rizvi.jobee.dtos.interview;

import lombok.Data;

@Data
public class InterviewPrepFeedbackDto {
    public InterviewPrepFeedbackDto(Integer reviewRating, String reviewText) {
        this.reviewRating = reviewRating;
        this.reviewText = reviewText;
    }

    private Integer reviewRating;
    private String reviewText;
}
