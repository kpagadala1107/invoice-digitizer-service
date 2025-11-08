package com.kp.invoice.service;

import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.models.*;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class OcrServiceDefault {

    @Value("${azure.api.endpoint}")
    private String azureEndpoint;

    @Value("${azure.api.key}")
    private String azureApiKey;

    @Autowired
    private LLMService llmService;

    private DocumentAnalysisClient client;

    @PostConstruct
    public void init() {
        this.client = new DocumentAnalysisClientBuilder()
                .credential(new AzureKeyCredential(azureApiKey))
                .endpoint(azureEndpoint)
                .buildClient();
    }

    public String extractFromImage(MultipartFile file) {
        try {
            AnalyzeResult result = client.beginAnalyzeDocument(
                    "prebuilt-document",
                    BinaryData.fromBytes(file.getBytes())
            ).getFinalResult();

            // --- 4️⃣ Call LLM Service with document content ---
            String documentContent = result.getContent();
            System.out.println("Document Content: \n" + documentContent);
            String llmExtractedFields = llmService.analyzeDocument(documentContent);

            // Clean and format the JSON response
            String cleanedJson = cleanJsonResponse(llmExtractedFields);
            System.out.println("LLM Extracted Fields (Clean JSON): \n" + cleanedJson);

            return cleanedJson;
        } catch (Exception e) {
            throw new RuntimeException("OCR failed: " + e.getMessage(), e);
        }
    }

    private String cleanJsonResponse(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            return "{}";
        }

        // Remove markdown code block markers if present
        String cleaned = jsonResponse.replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();

        // Remove escape characters and unwanted characters
        cleaned = cleaned.replaceAll("\\\\\"", "\"")     // Remove escaped quotes
                .replaceAll("\\\\n", "")                  // Remove escaped newlines
                .replaceAll("\\\\r", "")                  // Remove escaped carriage returns
                .replaceAll("\\\\t", " ")                 // Replace escaped tabs with spaces
                .replaceAll("\\\\\\\\", "")               // Remove escaped backslashes
                .replaceAll("\\\\", "")                   // Remove any remaining backslashes
                .replaceAll("\\n", " ")                   // Replace actual newlines with spaces
                .replaceAll("\\r", " ");                  // Replace actual carriage returns with spaces

        // Clean up spacing around JSON punctuation
        cleaned = cleaned.replaceAll("\\s*,\\s*", ",")
                .replaceAll("\\s*:\\s*", ":")
                .replaceAll("\\s*\\{\\s*", "{")
                .replaceAll("\\s*\\}\\s*", "}")
                .replaceAll("\\s*\\[\\s*", "[")
                .replaceAll("\\s*\\]\\s*", "]")
                .replaceAll("\\s+", " ")                  // Replace multiple spaces with single space
                .trim();

        // Ensure proper JSON formatting with minimal spacing
        cleaned = cleaned.replaceAll(",", ", ")
                .replaceAll(":", ": ")
                .replaceAll("\\{", "{ ")
                .replaceAll("\\}", " }")
                .replaceAll("\\[", "[ ")
                .replaceAll("\\]", " ]");

        return cleaned;
    }


}
