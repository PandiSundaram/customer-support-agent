package com.pandi.ai.agenticClientAI.service;

import com.pandi.ai.agenticClientAI.config.InboxProperties;
import com.pandi.ai.agenticClientAI.model.AgenticResponse;
import com.pandi.ai.agenticClientAI.model.IncomingEmail;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class SupportAgent {


    private final ChatClient chatClient;

    public SupportAgent(ChatClient.Builder chatClientBuilder,
                        ToolCallbackProvider mcpTools,
                        InboxProperties inbox,
                        @Value("classpath:/prompts/support-agent-system.st") Resource systemPrompt) {
        this.chatClient = chatClientBuilder
                // Give the model the verbatim instructions for how to work the mailbox,
                // injecting the support address it is acting on behalf of.
                .defaultSystem(sys -> sys.text(systemPrompt)
                        .param("support_address", inbox.getAddress()))
                // Expose every tool the MCP server publishes. Spring AI auto-executes
                // these in a loop, so the LLM can take as many steps as it needs.
                .defaultTools(mcpTools)
                .build();
    }

    /**
     * Hand a single email to the LLM and let it resolve the case end to end.
     *
     * @return the agent's structured outcome: the reply to send to the customer
     *         plus an internal summary of what it understood and did.
     */
    public AgenticResponse resolve(IncomingEmail email) {
        return chatClient.prompt()
                .user(u -> u.text("""
                        A new email just arrived in the support inbox. Resolve it.

                        From       : {from}
                        To         : {to}
                        Received   : {receivedAt}
                        Subject    : {subject}

                        Body:
                        {body}
                        """)
                        .param("from", email.from())
                        .param("to", String.join(", ", email.to()))
                        .param("receivedAt", email.receivedAt())
                        .param("subject", email.subject())
                        .param("body", email.body()))
                .call()
                .entity(AgenticResponse.class);
    }
}
