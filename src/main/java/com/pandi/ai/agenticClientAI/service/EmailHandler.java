package com.pandi.ai.agenticClientAI.service;

import com.pandi.ai.agenticClientAI.model.IncomingEmail;

@FunctionalInterface
public interface EmailHandler {

    boolean handle(IncomingEmail email);
}
