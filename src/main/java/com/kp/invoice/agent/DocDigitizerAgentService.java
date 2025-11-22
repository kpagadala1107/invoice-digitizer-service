package com.kp.invoice.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kp.invoice.agent.tools.AgentTool;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Main AI Agent Service - "Doc Digitizer Agent"
 * Orchestrates all tools and handles conversations with users
 */
@Service
public class DocDigitizerAgentService {

    private static final Logger logger = LoggerFactory.getLogger(DocDigitizerAgentService.class);

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${openai.model:gpt-4}")
    private String model;

    private final List<AgentTool> tools;
    private final ObjectMapper objectMapper;

    private static final String AGENT_SYSTEM_PROMPT = """
            You are "Doc Digitizer", an intelligent AI agent specialized in document processing and analysis.
            
            Your capabilities include:
            1. Extract raw data from documents using Azure Document Intelligence
            2. Convert extracted data to JSON format
            3. Convert extracted data to XML format
            4. Retrieve existing records from the database for businesses or individuals
            5. Validate document data for completeness and accuracy
            
            Your role is to help users process, analyze, and manage documents efficiently.
            When users ask you to perform tasks:
            - Describe which tool would be best for the task
            - Provide clear, concise responses
            - If multiple steps are needed, explain what should be done
            - Always recommend validation when appropriate
            
            Available Tools:
            - extract_raw_data_azure: Extract data from documents using Azure Document Intelligence
            - convert_to_json: Convert document data to JSON format
            - convert_to_xml: Convert document data to XML format
            - retrieve_database_record: Search and retrieve existing records from database
            - validate_document: Validate document data for correctness and completeness
            
            Be helpful, accurate, and efficient in your responses.
            """;

    public DocDigitizerAgentService(List<AgentTool> tools, ObjectMapper objectMapper) {
        this.tools = tools;
        this.objectMapper = objectMapper;
    }

    /**
     * Process a user request using the AI agent
     * For now, uses direct tool execution. Can be enhanced with function calling later.
     */
    public String processRequest(String userMessage, Map<String, Object> context) {
        try {
            // Check if the user is requesting a specific tool action
            String toolResult = tryDirectToolExecution(userMessage, context);
            if (toolResult != null) {
                return toolResult;
            }

            // Otherwise use LLM for general conversation
            OpenAiService service = new OpenAiService(openAiApiKey, Duration.ofSeconds(120));

            String toolsDescription = buildToolsDescription();
            String enhancedPrompt = AGENT_SYSTEM_PROMPT + "\n\n" + toolsDescription;

            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), enhancedPrompt));
            messages.add(new ChatMessage(ChatMessageRole.USER.value(), userMessage));
            messages.add(new ChatMessage(ChatMessageRole.USER.value(),
                    "Context Data: " + (context != null ? objectMapper.writeValueAsString(context.get("context")) : "{}")));

            ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(model)
                .messages(messages)
                .temperature(0.3)
                .maxTokens(2000)
                .build();

            ChatCompletionResult result = service.createChatCompletion(request);
            return result.getChoices().get(0).getMessage().getContent();

        } catch (Exception e) {
            logger.error("Error processing request", e);
            return "I apologize, but I encountered an error processing your request: " + e.getMessage();
        }
    }

    /**
     * Try to execute tool directly based on user message keywords
     */
    private String tryDirectToolExecution(String userMessage, Map<String, Object> context) {
        String lowerMessage = userMessage.toLowerCase();

        // Check for tool-specific keywords
        if (lowerMessage.contains("extract") || lowerMessage.contains("azure")) {
            if (context != null && context.containsKey("fileData")) {
                return executeTool("extract_raw_data_azure", context);
            }
        }

        if (lowerMessage.contains("convert") && lowerMessage.contains("json")) {
            if (context != null && context.containsKey("data")) {
                return executeTool("convert_to_json", context);
            }
        }

        if (lowerMessage.contains("convert") && lowerMessage.contains("xml")) {
            if (context != null && context.containsKey("data")) {
                return executeTool("convert_to_xml", context);
            }
        }

        if (lowerMessage.contains("search") || lowerMessage.contains("find") || lowerMessage.contains("retrieve")) {
            // Try to extract search parameters from message
            Map<String, Object> searchParams = extractSearchParameters(userMessage);
            if (searchParams != null) {
                return executeTool("retrieve_database_record", searchParams);
            }
        }

        if (lowerMessage.contains("validate")) {
            if (context != null && context.containsKey("documentData")) {
                return executeTool("validate_document", context);
            }
        }

        return null;
    }

    /**
     * Extract search parameters from user message
     */
    private Map<String, Object> extractSearchParameters(String message) {
        Map<String, Object> params = new HashMap<>();
        String lowerMessage = message.toLowerCase();

        if (lowerMessage.contains("vendor")) {
            params.put("searchType", "vendor");
            // Try to extract vendor name (simple implementation)
            if (lowerMessage.contains("\"")) {
                int start = message.indexOf("\"");
                int end = message.indexOf("\"", start + 1);
                if (end > start) {
                    params.put("searchValue", message.substring(start + 1, end));
                    return params;
                }
            }
        } else if (lowerMessage.contains("invoice number")) {
            params.put("searchType", "invoiceNumber");
        } else if (lowerMessage.contains("id")) {
            params.put("searchType", "id");
        } else if (lowerMessage.contains("all")) {
            params.put("searchType", "all");
            return params;
        }

        return params.isEmpty() ? null : params;
    }

    /**
     * Execute a specific tool
     */
    private String executeTool(String toolName, Map<String, Object> parameters) {
        Optional<AgentTool> tool = tools.stream()
            .filter(t -> t.getName().equals(toolName))
            .findFirst();

        if (tool.isPresent()) {
            return tool.get().execute(parameters);
        }

        return "{\"error\": \"Tool not found: " + toolName + "\"}";
    }

    /**
     * Build description of all available tools
     */
    private String buildToolsDescription() {
        StringBuilder sb = new StringBuilder("Available Tools:\n");
        for (AgentTool tool : tools) {
            sb.append("- ").append(tool.getName()).append(": ").append(tool.getDescription()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Get agent description and capabilities
     */
    public Map<String, Object> getAgentInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "Doc Digitizer");
        info.put("description", "An intelligent AI agent specialized in document processing and analysis");
        info.put("version", "1.0.0");

        List<Map<String, String>> capabilities = new ArrayList<>();
        for (AgentTool tool : tools) {
            Map<String, String> capability = new HashMap<>();
            capability.put("name", tool.getName());
            capability.put("description", tool.getDescription());
            capabilities.add(capability);
        }
        info.put("capabilities", capabilities);

        return info;
    }

    /**
     * List all available tools
     */
    public List<Map<String, Object>> listTools() {
        return tools.stream()
            .map(tool -> {
                Map<String, Object> toolInfo = new HashMap<>();
                toolInfo.put("name", tool.getName());
                toolInfo.put("description", tool.getDescription());
                toolInfo.put("parameters", tool.getParametersSchema());
                return toolInfo;
            })
            .collect(Collectors.toList());
    }
}

