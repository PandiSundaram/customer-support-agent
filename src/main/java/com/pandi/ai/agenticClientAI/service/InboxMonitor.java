package com.pandi.ai.agenticClientAI.service;

import com.pandi.ai.agenticClientAI.client.MailpitClient;
import com.pandi.ai.agenticClientAI.config.InboxProperties;
import com.pandi.ai.agenticClientAI.dto.MailpitAddress;
import com.pandi.ai.agenticClientAI.dto.MailpitMessage;
import com.pandi.ai.agenticClientAI.dto.MailpitMessageSummary;
import com.pandi.ai.agenticClientAI.model.IncomingEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class InboxMonitor {

    private static final Logger log = LoggerFactory.getLogger(InboxMonitor.class);

    private final MailpitClient mailpit;
    private final InboxProperties props;
    private final EmailHandler handler;

    public InboxMonitor(MailpitClient mailpit, InboxProperties props, EmailHandler handler) {
        this.mailpit = mailpit;
        this.props = props;
        this.handler = handler;
    }

    @Scheduled(fixedDelayString  = "${support-agent.inbox.poll-interval:10000}")
    public void poll() {
        try {
            List<MailpitMessageSummary> unread = mailpit.listUnread(props.getBatchSize());;
            if (unread.isEmpty()) {
                log.debug("No new mail");
                return;
            }
            log.info("Found {} new message(s)", unread.size());
            for (MailpitMessageSummary summary : unread) {
                processOne(summary.id());
            }
        } catch (Exception e) {
            // Mailpit may be starting up or briefly unreachable. Log and let the
            // next scheduled poll retry rather than killing the scheduler.
            log.warn("Inbox poll failed: {}", e.getMessage(), e);
        }
    }

    private void processOne(String id) {
        try {
            // Fetching the full message marks it read on the server.
            MailpitMessage message = mailpit.getMessage(id);
            IncomingEmail email = toIncomingEmail(message);
            boolean handled = handler.handle(email);
            if (!handled) {
                // Leave it unread so the next poll picks it up again.
                mailpit.setRead(id, false);
            }
        } catch (Exception e) {
            log.error("Failed to process message {}; resetting to unread for retry", id, e);
            try {
                mailpit.setRead(id, false);
            } catch (Exception reset) {
                log.warn("Could not reset message {} to unread: {}", id, reset.getMessage());
            }
        }
    }

    private IncomingEmail toIncomingEmail(MailpitMessage message) {
        String from = message.from() != null ? message.from().address() : "(unknown)";
        List<String> to = message.to().stream()
                .map(MailpitAddress::address)
                .toList();
        String subject = message.subject() != null ? message.subject() : "";
        String body = bestBody(message);
        Instant receivedAt = parseDate(message.date());
        return new IncomingEmail(message.messageId(), from, to, subject, body, receivedAt);
    }
    private Instant parseDate(String date) {
        if (date == null || date.isBlank()) {
            return Instant.now();
        }
        try {
            return OffsetDateTime.parse(date).toInstant();
        } catch (Exception e) {
            try {
                return Instant.parse(date);
            } catch (Exception ex) {
                return Instant.now();
            }
        }
    }

    private String bestBody(MailpitMessage message) {
        if (message.text() != null && !message.text().isBlank()) {
            return message.text().strip();
        }
        return message.html() != null ? message.html().strip() : "";
    }

}
