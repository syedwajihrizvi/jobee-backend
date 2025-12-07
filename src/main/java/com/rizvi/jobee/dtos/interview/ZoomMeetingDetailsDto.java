package com.rizvi.jobee.dtos.interview;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

@Data
public class ZoomMeetingDetailsDto {
    private String meetingId;
    private String startUrl;
    private String joinUrl;
    private String meetingPassword;
    private String timezone;
    private List<ZoomMeetingRegistrantDto> registrants;
    private Boolean needToSendEmailInvites;

    public JsonNode toJsonNode(ObjectMapper mapper) {
        return mapper.valueToTree(this);
    }
}
