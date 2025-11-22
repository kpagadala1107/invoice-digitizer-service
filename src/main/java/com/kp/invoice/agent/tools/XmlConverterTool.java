package com.kp.invoice.agent.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Tool for converting extracted data to XML format
 */
@Component
public class XmlConverterTool implements AgentTool {

    private final XmlMapper xmlMapper;
    private final ObjectMapper objectMapper;

    public XmlConverterTool(ObjectMapper objectMapper) {
        this.xmlMapper = new XmlMapper();
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "convert_to_xml";
    }

    @Override
    public String getDescription() {
        return "Converts document data to properly formatted XML. " +
               "Can parse raw text, JSON, or structured data and convert it to XML format. " +
               "Useful for legacy system integration or specific XML requirements.";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();

        Map<String, Object> data = new HashMap<>();
        data.put("type", "string");
        data.put("description", "The data to convert to XML format. Can be raw text, JSON, or key-value pairs.");
        properties.put("data", data);

        Map<String, Object> rootElement = new HashMap<>();
        rootElement.put("type", "string");
        rootElement.put("description", "The name of the root XML element");
        rootElement.put("default", "document");
        properties.put("rootElement", rootElement);

        schema.put("properties", properties);
        schema.put("required", new String[]{"data"});

        return schema;
    }

    @Override
    public String execute(Map<String, Object> parameters) {
        try {
            Object data = parameters.get("data");
            String rootElement = (String) parameters.getOrDefault("rootElement", "document");

            // If data is a string, try to parse it
            if (data instanceof String) {
                String dataStr = (String) data;
                try {
                    // Try to parse as JSON first
                    data = objectMapper.readValue(dataStr, Object.class);
                } catch (Exception e) {
                    // If not JSON, try to parse as key-value pairs
                    data = parseKeyValuePairs(dataStr);
                }
            }

            // Wrap data in root element
            Map<String, Object> wrappedData = new HashMap<>();
            wrappedData.put(rootElement, data);

            // Convert to XML
            String xml = xmlMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(wrappedData);

            return xml;

        } catch (Exception e) {
            return createErrorResponse("Failed to convert to XML: " + e.getMessage());
        }
    }

    private Map<String, String> parseKeyValuePairs(String text) {
        Map<String, String> result = new HashMap<>();
        String[] lines = text.split("\n");

        for (String line : lines) {
            if (line.contains(":")) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    result.put(parts[0].trim().replaceAll("[^a-zA-Z0-9]", "_"), parts[1].trim());
                }
            }
        }

        return result;
    }

    private String createErrorResponse(String message) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><error>" + message + "</error>";
    }
}

