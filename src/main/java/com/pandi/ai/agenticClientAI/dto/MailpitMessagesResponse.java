package com.pandi.ai.agenticClientAI.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record MailpitMessagesResponse(@JsonProperty("total") int total,
                                      @JsonProperty("unread") int unread,
                                      @JsonProperty("count") int count,
                                      @JsonProperty("messages") List<MailpitMessageSummary> messages) {
    public List<MailpitMessageSummary> messages() {
        return messages != null ? messages : List.of();
    }
}
