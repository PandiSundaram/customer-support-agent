package com.pandi.ai.agenticClientAI.model;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@JsonClassDescription("The outcome of handling a support email: the reply to send to the customer plus an internal summary.")
public record AgenticResponse(@JsonPropertyDescription("Subject line for the reply email to the customer, e.g. \"Re: Charged twice for order 4471\".")
                               String replySubject,

                              @JsonPropertyDescription("The complete, ready-to-send reply to the customer, written in their language and tone. "
                                       + "Plain text, properly greeting and signing off. No placeholders or bracketed TODOs.")
                               String replyBody,

                              @JsonPropertyDescription("A short internal summary for the human operator: who the customer was, what they "
                                       + "wanted, what you found, and what action you took (include any refund id / ticket id).")
                               String operatorSummary) {
}
