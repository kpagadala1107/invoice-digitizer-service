package com.kp.invoice.agent.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Tool for converting extracted data to JSON format
 */
@Component
public class JsonConverterTool implements AgentTool {

    private final ObjectMapper objectMapper;

    public JsonConverterTool(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "convert_to_json";
    }

    @Override
    public String getDescription() {
        return "Converts document data to properly formatted JSON. " +
               "Can parse raw text or structured data and convert it to clean JSON format. " +
               "Useful for standardizing output format.";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();

        Map<String, Object> data = new HashMap<>();
        data.put("type", "string");
        data.put("description", "The data to convert to JSON format. Can be raw text, key-value pairs, or existing structured data.");
        properties.put("data", data);

        Map<String, Object> prettyPrint = new HashMap<>();
        prettyPrint.put("type", "boolean");
        prettyPrint.put("description", "Whether to pretty print the JSON output");
        prettyPrint.put("default", true);
        properties.put("prettyPrint", prettyPrint);

        schema.put("properties", properties);
        schema.put("required", new String[]{"data"});

        return schema;
    }

    @Override
    public String execute(Map<String, Object> parameters) {
        try {
            Object data = parameters.get("data");
            Boolean prettyPrint = (Boolean) parameters.getOrDefault("prettyPrint", true);

            // If data is already a string, try to parse it
            if (data instanceof String) {
                String dataStr = (String) data;
                try {
                    // Try to parse as JSON first
                    Object parsedData = objectMapper.readValue(dataStr, Object.class);
                    data = parsedData;
                } catch (Exception e) {
                    // If not JSON, try to parse as key-value pairs
                    data = parseKeyValuePairs(dataStr);
                }
            }

            // Convert to JSON
            if (prettyPrint) {
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
            } else {
                return objectMapper.writeValueAsString(data);
            }

        } catch (Exception e) {
            return createErrorResponse("Failed to convert to JSON: " + e.getMessage());
        }
    }

    private Map<String, String> parseKeyValuePairs(String text) {
        Map<String, String> result = new HashMap<>();
        String[] lines = text.split("\n");

        for (String line : lines) {
            if (line.contains(":")) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    result.put(parts[0].trim(), parts[1].trim());
                }
            }
        }

        return result;
    }

    private String createErrorResponse(String message) {
        try {
            Map<String, String> error = new HashMap<>();
            error.put("error", message);
            return objectMapper.writeValueAsString(error);
        } catch (Exception e) {
            return "{\"error\": \"" + message + "\"}";
        }
    }
}

