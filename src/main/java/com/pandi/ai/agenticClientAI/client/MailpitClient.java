package com.pandi.ai.agenticClientAI.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pandi.ai.agenticClientAI.config.InboxProperties;
import com.pandi.ai.agenticClientAI.dto.MailpitMessage;
import com.pandi.ai.agenticClientAI.dto.MailpitMessageSummary;
import com.pandi.ai.agenticClientAI.dto.MailpitMessagesResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class MailpitClient {

    private final RestClient rest;
    private final String inboxAddress;

    public MailpitClient(RestClient.Builder builder, InboxProperties props) {
        this.rest = builder.baseUrl(props.getBaseUrl()).build();
        this.inboxAddress = props.getAddress();
    }

    public List<MailpitMessageSummary> listUnread(int limit) {
        String query = "is:unread to:%s !from:%s".formatted(inboxAddress, inboxAddress);
        MailpitMessagesResponse response = rest.get()
                .uri(uri -> uri.path("/api/v1/search")
                        .queryParam("query", query)
                        .queryParam("limit", limit)
                        .build())
                .retrieve()
                .body(MailpitMessagesResponse.class);
        return response != null ? response.messages() : List.of();
    }

    /**
     * Fetch a full message by id. Side effect: Mailpit marks the message as read.
     */
    public MailpitMessage getMessage(String id) {
        return rest.get()
                .uri("/api/v1/message/{id}", id)
                .retrieve()
                .body(MailpitMessage.class);
    }


    public void setRead(String id, boolean read) {
        rest.put()
                .uri("/api/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ReadUpdate(List.of(id), read))
                .retrieve()
                .toBodilessEntity();
    }

    /** Request body for {@code PUT /api/v1/messages}. */
    private record ReadUpdate(
            @JsonProperty("IDs") List<String> ids,
            @JsonProperty("Read") boolean read
    ) {
    }

}
