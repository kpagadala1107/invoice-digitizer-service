package com.kp.invoice.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for AI Agent interactions
 */
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final DocDigitizerAgentService agentService;
    private final ObjectMapper objectMapper;

    public AgentController(DocDigitizerAgentService agentService, ObjectMapper objectMapper) {
        this.agentService = agentService;
        this.objectMapper = objectMapper;
    }

    /**
     * Get agent information and capabilities
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getAgentInfo() {
        return ResponseEntity.ok(agentService.getAgentInfo());
    }

    /**
     * List all available tools
     */
    @GetMapping("/tools")
    public ResponseEntity<?> listTools() {
        return ResponseEntity.ok(agentService.listTools());
    }

    /**
     * Chat with the agent
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, Object> request) {
        String message = request.get("message").toString();

        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Message is required"));
        }

        String response = agentService.processRequest(message, request);

        Map<String, String> result = new HashMap<>();
        result.put("response", response);
        result.put("agent", "Doc Digitizer");

        return ResponseEntity.ok(result);
    }

    /**
     * Process a document with the agent
     */
    @PostMapping("/process-document")
    public ResponseEntity<Map<String, String>> processDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "instruction", required = false, defaultValue = "Extract and analyze this document") String instruction) {

        try {
            // Convert file to base64 or bytes for context
            Map<String, Object> context = new HashMap<>();
            context.put("fileData", file);
            context.put("fileName", file.getOriginalFilename());
            context.put("fileType", file.getContentType());

            // Create a message that includes the instruction and file info
            String message = String.format(
                "%s. File: %s (type: %s, size: %d bytes)",
                instruction,
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize()
            );

            String response = agentService.processRequest(message, context);

            Map<String, String> result = new HashMap<>();
            result.put("response", response);
            result.put("fileName", file.getOriginalFilename());
            result.put("agent", "Doc Digitizer");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to process document: " + e.getMessage()));
        }
    }

    /**
     * Execute a specific tool directly (for testing/debugging)
     */
    @PostMapping("/execute-tool")
    public ResponseEntity<Map<String, String>> executeTool(@RequestBody Map<String, Object> request) {
        String toolName = (String) request.get("toolName");
        String parameters = (String) request.get("parameters");

        if (toolName == null || parameters == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "toolName and parameters are required"));
        }

        // Create a message asking the agent to use the specific tool
        String message = String.format(
            "Use the %s tool with these parameters: %s",
            toolName,
            parameters
        );

        String response = agentService.processRequest(message, null);

        Map<String, String> result = new HashMap<>();
        result.put("response", response);
        result.put("toolName", toolName);

        return ResponseEntity.ok(result);
    }

    /**
     * Validate a document
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, String>> validateDocument(@RequestBody Map<String, Object> request) {
        String documentData = (String) request.get("documentData");
        String validationType = (String) request.getOrDefault("validationType", "general");

        if (documentData == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "documentData is required"));
        }

        String message = String.format(
            "Validate this %s document: %s",
            validationType,
            documentData
        );

        String response = agentService.processRequest(message, null);

        Map<String, String> result = new HashMap<>();
        result.put("response", response);
        result.put("validationType", validationType);

        return ResponseEntity.ok(result);
    }

    /**
     * Search database
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, String>> searchDatabase(
            @RequestParam String searchType,
            @RequestParam(required = false) String searchValue) {

        String message = searchValue != null ?
            String.format("Search the database by %s for '%s'", searchType, searchValue) :
            String.format("Search the database for all records (type: %s)", searchType);

        String response = agentService.processRequest(message, null);

        Map<String, String> result = new HashMap<>();
        result.put("response", response);
        result.put("searchType", searchType);

        return ResponseEntity.ok(result);
    }
}

