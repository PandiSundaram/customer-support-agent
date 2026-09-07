package com.pandi.ai.agenticClientAI.model;

import java.time.Instant;
import java.util.List;

public record IncomingEmail(String messageId,
                            String from,
                            List<String> to,
                            String subject,
                            String body,
                            Instant receivedAt) {
}
