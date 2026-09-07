package com.pandi.ai.agenticClientAI.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MailpitMessageSummary(@JsonProperty("ID") String id,
                                    @JsonProperty("MessageID") String messageId,
                                    @JsonProperty("Read") boolean read,
                                    @JsonProperty("From") MailpitAddress from,
                                    @JsonProperty("Subject") String subject,
                                    @JsonProperty("Created") String created) {
}
