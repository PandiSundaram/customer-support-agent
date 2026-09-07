package com.pandi.ai.agenticClientAI.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MailpitAddress(@JsonProperty("Name") String name,
                             @JsonProperty("Address") String address) {
}
