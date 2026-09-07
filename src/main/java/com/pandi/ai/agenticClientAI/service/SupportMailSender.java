package com.pandi.ai.agenticClientAI.service;

import com.pandi.ai.agenticClientAI.config.InboxProperties;
import com.pandi.ai.agenticClientAI.model.AgenticResponse;
import com.pandi.ai.agenticClientAI.model.IncomingEmail;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class SupportMailSender {


    private static final Logger log = LoggerFactory.getLogger(SupportMailSender.class);

    /** "On Wed, 11 Jun 2026 at 14:03, ... wrote:" — the usual quoted-reply attribution line. */
    private static final DateTimeFormatter QUOTE_DATE =
            DateTimeFormatter.ofPattern("EEE, d MMM yyyy 'at' HH:mm", Locale.ENGLISH)
                    .withZone(ZoneId.systemDefault());

    private final JavaMailSender mailSender;
    private final InboxProperties inbox;

    public SupportMailSender(JavaMailSender mailSender, InboxProperties inbox) {
        this.mailSender = mailSender;
        this.inbox = inbox;
    }


    public boolean sendReply(IncomingEmail original, AgenticResponse response) throws Exception {
        String recipient = original.from();
        if (recipient == null || recipient.isBlank() || "(unknown)".equals(recipient)) {
            log.warn("No usable sender address on email \"{}\"; skipping reply", original.subject());
            return false;
        }
        if (response.replyBody() == null || response.replyBody().isBlank()) {
            log.warn("Agent produced no reply body for email from {}; skipping reply", recipient);
            return false;
        }

        MimeMessage mime = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mime, false, "UTF-8");
        helper.setFrom(inbox.getAddress());
        helper.setTo(recipient);
        helper.setSubject(replySubject(original, response));
        helper.setText(quoteOriginal(original, response.replyBody()));

        // Thread the reply onto the original message when we know its Message-ID.
        String messageId = original.messageId();
        if (messageId != null && !messageId.isBlank()) {
            String ref = messageId.startsWith("<") ? messageId : "<" + messageId + ">";
            mime.setHeader("In-Reply-To", ref);
            mime.setHeader("References", ref);
        }

        mailSender.send(mime);
        log.info("Replied to {} (subject: \"{}\")", recipient, mime.getSubject());
        return true;
    }

    /**
     * Build the reply subject from the original ("Re: <subject>"), which — together
     * with the In-Reply-To/References headers — is what makes mail clients group the
     * reply into the original conversation instead of showing a fresh thread.
     */
    private String replySubject(IncomingEmail original, AgenticResponse response) {
        String base = original.subject() != null && !original.subject().isBlank()
                ? original.subject()
                : (response.replySubject() != null ? response.replySubject() : "");
        return base.regionMatches(true, 0, "Re:", 0, 3) ? base : "Re: " + base;
    }

    /**
     * Append the customer's original message under the agent's reply as a quoted
     * block ("On &lt;date&gt;, &lt;sender&gt; wrote: &gt; ..."), the way a normal mail
     * client does, so the reply carries the context it is responding to.
     */
    private String quoteOriginal(IncomingEmail original, String replyBody) {
        String body = original.body() != null ? original.body() : "";
        String quoted = body.isBlank()
                ? ""
                : body.stripTrailing().lines().map(line -> "> " + line).reduce((a, b) -> a + "\n" + b).orElse("");
        return """
                %s

                On %s, %s wrote:
                %s""".formatted(
                replyBody.stripTrailing(),
                QUOTE_DATE.format(original.receivedAt()),
                original.from(),
                quoted);
    }
}
