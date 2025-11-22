package com.rizvi.jobee.helpers.AISchemas;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class AISocialMedia {
    @JsonPropertyDescription("Type of social media platform")
    public String type;
    @JsonPropertyDescription("URL to the social media profile")
    public String url;

    public String toJsonString() {
        return """
                {"type": "%s", "url": "%s"}
                """.formatted(type, url);
    }

}
