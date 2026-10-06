# Customer Support Agent

The application uses an LLM-powered agent to understand customer requests, reason about the required actions, select the appropriate tools, and use the returned information to determine the next step.

The MCP server provides the tools that the agent can invoke. The LLM is responsible for deciding **which tool to call and when**, while the MCP server is responsible for **executing those tool calls**.

---

# Flow Diagram

```text
                         Customer Email
                               │
                               ▼
                       ┌───────────────┐
                       │   AI Agent    │
                       │    :8080      │
                       └───────┬───────┘
                               │
                               ▼
                       ┌───────────────┐
                       │      LLM      │
                       │ Understand    │
                       │ Reason        │
                       │ Decide        │
                       └───────┬───────┘
                               │
                               ▼
                       Select MCP Tool
                               │
                               ▼
                       ┌───────────────┐
                       │  MCP Server   │
                       │    :8090      │
                       └───────┬───────┘
                               │
                               ▼
                    Business Logic / MySQL
                               │
                               ▼
                         Tool Result
                               │
                               ▼
                       ┌───────────────┐
                       │      LLM      │
                       │ Evaluate      │
                       │ Tool Result   │
                       └───────┬───────┘
                               │
                         Need more?
                         /         \
                       YES         NO
                        │           │
                        ▼           ▼
                  Another Tool   Final Response
                        │
                        └──────────► LLM
```

The key part of the flow is the **agentic loop**:

```text
        ┌─────────────────────────────┐
        │                             │
        ▼                             │
      LLM                             │
        │                             │
        ▼                             │
   Select Tool                        │
        │                             │
        ▼                             │
   MCP Server                         │
        │                             │
        ▼                             │
   Tool Result                        │
        │                             │
        ▼                             │
      LLM ────── Need more? ── YES ──┘
        │
        └──────────── NO ───────────► Response
```

The LLM can decide to invoke another tool based on the result of the previous tool call. This allows different customer requests to follow different tool paths.

---

# Seed a Test Customer Email

The `support-agent` service provides a `/seed-mail` endpoint to inject a test customer email into the support inbox.

This can be used to test the complete agentic workflow locally.

## Endpoint

```http
POST http://localhost:8080/seed-mail
```

## Request Parameters

| Parameter | Default                                 | Description            |
| --------- | --------------------------------------- | ---------------------- |
| `from`    | `customer@example.com`                  | Customer email address |
| `subject` | `Test support request`                  | Email subject          |
| `body`    | `Hi, I need help with my recent order.` | Customer's request     |

## Example — Duplicate Charge

This example can be used to test the agent's ability to identify a duplicate payment and initiate a refund.

```bash
curl -G "http://localhost:8080/seed-mail" \
  --data-urlencode "from=customer@example.com" \
  --data-urlencode "subject=Charged Twice for My Order" \
  --data-urlencode "body=Hi, I was charged twice for my order. Please check the duplicate charge and refund it."
```

The customer email will contain:

```text
From: customer@example.com

Subject: Charged Twice for My Order

Hi,

I was charged twice for my order.
Please check the duplicate charge and refund it.
```

### Expected Agent Flow

```text
Customer Email
      │
      ▼
   AI Agent
      │
      ▼
     LLM
      │
      │ Understand:
      │ "Customer was charged twice"
      ▼
get_customer()
      │
      ▼
get_order()
      │
      ▼
detect_duplicate_charge()
      │
      ▼
 MCP Server
      │
      ▼
    MySQL
      │
      ▼
Duplicate confirmed
      │
      ▼
 issue_refund()
      │
      ▼
log_resolution()
      │
      ▼
     LLM
      │
      ▼
Customer Response
```

The important point is that the **LLM decides which tools are required based on the customer's request and the results returned by previous tool calls**.

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

## Current Status

**Status:** Local Development

The application and `customer-support-mcp` server currently run locally.

The MCP server is **not currently published to the MCP Registry**. MCP Inspector is used to discover and test the locally exposed tools.
