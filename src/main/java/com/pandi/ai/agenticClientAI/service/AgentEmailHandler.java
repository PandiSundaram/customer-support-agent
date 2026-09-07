package com.pandi.ai.agenticClientAI.service;

import com.pandi.ai.agenticClientAI.model.AgenticResponse;
import com.pandi.ai.agenticClientAI.model.IncomingEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class AgentEmailHandler implements EmailHandler {

    private static final Logger log = LoggerFactory.getLogger(AgentEmailHandler.class);

    private final SupportAgent agent;
    private final SupportMailSender mailSender;

    public AgentEmailHandler(SupportAgent agent, SupportMailSender mailSender) {
        this.agent = agent;
        this.mailSender = mailSender;
    }

    @Override
    public boolean handle(IncomingEmail email) {
        log.info("Handing email from {} (subject: \"{}\") to the support agent", email.from(), email.subject());
        try {
            AgenticResponse response = agent.resolve(email);
            log.info("""
                    === Agent resolution ===
                    From    : {}
                    Subject : {}
                    Outcome :
                    {}
                    ========================""",
                    email.from(), email.subject(), response.operatorSummary());
            // Send the drafted reply back to the customer, threaded onto their email.
            mailSender.sendReply(email, response);
            return true;
        } catch (Exception e) {
            // Anything from a transient LLM/MCP error to a tool failure lands here.
            // Leave the mail unread so the next poll retries it.
            log.error("Agent failed to resolve email from {} (subject: \"{}\"); will retry",
                    email.from(), email.subject(), e);
            return false;
        }
    }

}
