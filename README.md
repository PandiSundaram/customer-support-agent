#  Customer Support Agent

The application uses an LLM-powered agent to understand customer requests, reason about the required actions, select the appropriate tools, and use the returned information to determine the next step.

The MCP server provides the tools that the agent can invoke. The LLM is responsible for deciding which tool to call and when, while the MCP server is responsible for executing those tool calls.

---
# Flow diagram
            
          Customer Email
                ↓
            AI Agent
                ↓
   LLM: Understand + Reason + Decide
                ↓
        Select  MCP Tool
                ↓
          MCP Server
                ↓
          Business Logic / MySQL
                ↓
          Tool Result
                ↓
          LLM: Evaluate Result
                ↓
         ┌───────────────┐
         │ Need more?    │
         └───────┬───────┘
        YES│            │NO
           ↓            ↓
    Another Tool        Final Response
           │
           └──────→ LLM


---

#  Project Goals

This project demonstrates practical implementation of:

* AI Agents
* Agentic workflows
* LLM tool calling
* Model Context Protocol
* MCP servers
* Spring AI
* Database-backed AI applications
* Autonomous support workflows
* AI-driven decision making
* Integration of AI with existing backend services

The main objective is to demonstrate how an AI agent can move beyond **conversation and text generation** and interact with real application capabilities through tools.

---

#  Future Improvements

Potential future enhancements include:

* Authentication and authorization for MCP tools
* More customer-support tools
* Integration with real external services
* Persistent conversation memory
* Human approval for sensitive operations such as refunds
* Tool execution auditing
* Observability and tracing for agent/tool calls
* Deployment of the MCP server
* Publishing the MCP server to an MCP registry

---

## 📌 Current Status

**Status:**  Local Development

The application and `customer-support-mcp` server currently run locally.

The MCP server is **not currently published to the MCP Registry**. MCP Inspector is used to discover and test the locally exposed tools.
