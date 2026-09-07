package com.pandi.ai.agenticClientAI.controller;

import com.pandi.ai.agenticClientAI.config.InboxProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
public class SeedMailController {

    private final InboxProperties inbox;
    private final JavaMailSender mailSender;

    public SeedMailController(InboxProperties inbox, JavaMailSender mailSender) {
        this.inbox = inbox;
        this.mailSender = mailSender;
    }

    @PostMapping("/seed-mail")
    public ResponseEntity<SeedResult> seed(
            @RequestParam(defaultValue = "customer@example.com") String from,
            @RequestParam(defaultValue = "Test support request") String subject,
            @RequestParam(defaultValue = "Hi, I need help with my recent order.") String body) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(inbox.getAddress());
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
        } catch (MailException e) {
            // Usually means the Mailpit SMTP server (compose.yaml) isn't up yet.
            return ResponseEntity.status(502).body(SeedResult.failed(from, inbox.getAddress(), subject, e));
        }

        return ResponseEntity.ok(SeedResult.sent(from, inbox.getAddress(), subject, body));
    }

    public record SeedResult(
            String status,
            String message,
            String from,
            String to,
            String subject,
            String body,
            String error,
            Instant timestamp
    ) {
        static SeedResult sent(String from, String to, String subject, String body) {
            return new SeedResult("sent",
                    "Email delivered to %s; the agent will pick it up on the next inbox poll.".formatted(to),
                    from, to, subject, body, null, Instant.now());
        }

        static SeedResult failed(String from, String to, String subject, Exception e) {
            return new SeedResult("failed",
                    "Could not deliver email to %s. Is the Mailpit SMTP server running?".formatted(to),
                    from, to, subject, null, e.getMessage(), Instant.now());
        }
    }
}
