package com.rizvi.jobee.dtos.interview;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

@Data
public class GoogleMeetingDetailsDto {
    private String meetingId;
    private String eventId;
    private String hangoutLink;

    public JsonNode toJsonNode(ObjectMapper objectMapper) {
        return objectMapper.valueToTree(this);
    }
}
