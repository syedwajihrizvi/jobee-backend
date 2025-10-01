package com.rizvi.jobee.helpers.AISchemas;

import lombok.Data;

@Data
public class InterviewPrepQuestion {
    private String question;
    private String answer;

    public String toJsonString() {
        return """
                {"question": "%s", "answer": "%s"}
                """.formatted(question, answer);
    }
}
