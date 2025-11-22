package com.kp.invoice.agent.tools;

import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeResult;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentField;
import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kp.invoice.service.OcrServiceDefault;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tool for extracting raw data from Azure Document Intelligence
 */
@Component
public class AzureDocumentIntelligenceTool implements AgentTool {

    private final DocumentAnalysisClient client;
    private final ObjectMapper objectMapper;

    private final OcrServiceDefault ocrServiceDefault;

    public AzureDocumentIntelligenceTool(DocumentAnalysisClient client, ObjectMapper objectMapper, OcrServiceDefault ocrServiceDefault) {
        this.client = client;
        this.objectMapper = objectMapper;
        this.ocrServiceDefault = ocrServiceDefault;
    }

    @Override
    public String getName() {
        return "extract_raw_data_azure";
    }

    @Override
    public String getDescription() {
        return "Extracts raw data from documents using Azure Document Intelligence. " +
               "Supports invoices, receipts, identity documents, and custom forms. " +
               "Returns structured field-value pairs extracted from the document.";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();

        Map<String, Object> fileData = new HashMap<>();
        fileData.put("type", "string");
        fileData.put("description", "Base64 encoded file data or file path");
        properties.put("fileData", fileData);

        Map<String, Object> modelType = new HashMap<>();
        modelType.put("type", "string");
        modelType.put("description", "Model type to use: prebuilt-invoice, prebuilt-receipt, prebuilt-document, etc.");
        modelType.put("default", "prebuilt-document");
        properties.put("modelType", modelType);

        schema.put("properties", properties);
        schema.put("required", new String[]{"fileData"});

        return schema;
    }

    @Override
    public String execute(Map<String, Object> parameters) {
        try {
            // Get the MultipartFile from the context
            Object fileDataObj = parameters.get("fileData");

            if (fileDataObj instanceof MultipartFile) {
                MultipartFile file = (MultipartFile) fileDataObj;
                return ocrServiceDefault.extractFromImage(file);
            } else {
                return createErrorResponse("Invalid file data. Expected MultipartFile.");
            }
//            byte[] fileData = (byte[]) parameters.get("fileData");
//            String modelType = (String) parameters.getOrDefault("modelType", "prebuilt-document");
//
//            AnalyzeResult result = client.beginAnalyzeDocument(
//                modelType,
//                BinaryData.fromBytes(fileData)
//            ).getFinalResult();
//
//            Map<String, Object> extractedData = new HashMap<>();
//
//            result.getDocuments().forEach(doc -> {
//                Map<String, DocumentField> fields = doc.getFields();
//                Map<String, Object> documentFields = new HashMap<>();
//
//                fields.forEach((key, field) -> {
//                    documentFields.put(key, extractFieldValue(field));
//                });
//
//                extractedData.put("documentType", doc.getDocType());
//                extractedData.put("confidence", doc.getConfidence());
//                extractedData.put("fields", documentFields);
//            });
//
//            // Add raw content
//            extractedData.put("content", result.getContent());
//
//            return objectMapper.writeValueAsString(extractedData);

        } catch (Exception e) {
            return createErrorResponse("Failed to extract data from Azure Document Intelligence: " + e.getMessage());
        }
    }

    private Object extractFieldValue(DocumentField field) {
        if (field == null) return null;

        try {
            // Try different value types based on what's available
            if (field.getValueAsString() != null) {
                return field.getValueAsString();
            }
            if (field.getValueAsDouble() != null) {
                return field.getValueAsDouble();
            }
            if (field.getValueAsLong() != null) {
                return field.getValueAsLong();
            }
            if (field.getValueAsDate() != null) {
                return field.getValueAsDate().toString();
            }
            if (field.getValueAsTime() != null) {
                return field.getValueAsTime().toString();
            }
            if (field.getValueAsCurrency() != null) {
                return field.getValueAsCurrency().getAmount();
            }
            if (field.getValueAsList() != null && !field.getValueAsList().isEmpty()) {
                return field.getValueAsList().stream()
                    .map(this::extractFieldValue)
                    .collect(java.util.stream.Collectors.toList());
            }
            if (field.getValueAsMap() != null && !field.getValueAsMap().isEmpty()) {
                Map<String, Object> mapValue = new HashMap<>();
                field.getValueAsMap().forEach((key, value) -> {
                    mapValue.put(key, extractFieldValue(value));
                });
                return mapValue;
            }
            return field.getContent();
        } catch (Exception e) {
            return field.getContent();
        }
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

