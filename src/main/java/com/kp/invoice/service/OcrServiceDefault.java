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
            System.out.println("\n---- LLM ANALYSIS ----");
            String documentContent = result.getContent();
            String llmExtractedFields = llmService.analyzeDocument(documentContent);


//            // --- 1️⃣ Extract Key-Value Pairs ---
//            System.out.println("---- KEY VALUE PAIRS ----");
//            List<DocumentKeyValuePair> keyValuePairs = result.getKeyValuePairs();
//            if (keyValuePairs != null) {
//                for (DocumentKeyValuePair kvp : keyValuePairs) {
//                    String key = kvp.getKey() != null ? kvp.getKey().getContent() : "";
//                    String value = kvp.getValue() != null ? kvp.getValue().getContent() : "";
//                    System.out.println(key + " : " + value);
//                }
//            }
//
//            // --- 2️⃣ Extract Tables ---
//            System.out.println("\n---- TABLES ----");
//            List<DocumentTable> tables = result.getTables();
//            if (tables != null) {
//                for (DocumentTable table : tables) {
//                    System.out.println("Table with " + table.getRowCount() + " rows and " +
//                            table.getColumnCount() + " columns");
//
//                    for (DocumentTableCell cell : table.getCells()) {
//                        System.out.printf("(%d,%d): %s%n",
//                                cell.getRowIndex(), cell.getColumnIndex(), cell.getContent());
//                    }
//                }
//            }
//
//            // --- 3️⃣ Extract Paragraphs ---
//            System.out.println("\n---- PARAGRAPHS ----");
//            List<DocumentParagraph> paragraphs = result.getParagraphs();
//            if (paragraphs != null) {
//                for (DocumentParagraph paragraph : paragraphs) {
//                    System.out.println(paragraph.getContent());
//                }
//            }

            return llmExtractedFields;


        } catch (Exception e) {
            throw new RuntimeException("OCR failed: " + e.getMessage(), e);
        }
    }
}
