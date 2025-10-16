package com.kp.invoice.service;

import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeResult;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentField;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.kp.invoice.dto.InvoiceDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OcrService {

    @Value("${azure.api.endpoint}")
    private String azureEndpoint;

    @Value("${azure.api.key}")
    private String azureApiKey;

    private DocumentAnalysisClient client;

    @PostConstruct
    public void init() {
        this.client = new DocumentAnalysisClientBuilder()
                .credential(new AzureKeyCredential(azureApiKey))
                .endpoint(azureEndpoint)
                .buildClient();
    }

    public InvoiceDTO extractFromImage(MultipartFile file) {
        try {
            AnalyzeResult result = client.beginAnalyzeDocument(
                    "prebuilt-invoice",
                    BinaryData.fromBytes(file.getBytes())
            ).getFinalResult();

            InvoiceDTO dto = new InvoiceDTO();
            dto.items = new ArrayList<>();

            result.getDocuments().forEach(doc -> {
                Map<String, DocumentField> fields = doc.getFields();

                if (fields.containsKey("InvoiceId"))
                    dto.invoiceNumber = fields.get("InvoiceId").getValueAsString();

                if (fields.containsKey("VendorName"))
                    dto.vendor = fields.get("VendorName").getValueAsString();

                if (fields.containsKey("InvoiceDate"))
                    dto.date = fields.get("InvoiceDate").getValueAsDate().toString();

                if (fields.containsKey("TotalTax"))
                    dto.tax = fields.get("TotalTax").getValueAsCurrency().getAmount();

                if (fields.containsKey("InvoiceTotal"))
                    dto.totalAmount = fields.get("InvoiceTotal").getValueAsCurrency().getAmount();

                if (fields.containsKey("Items")) {
                    List<DocumentField> items = fields.get("Items").getValueAsList();
                    for (DocumentField itemField : items) {
                        Map<String, DocumentField> itemMap = itemField.getValueAsMap();
                        InvoiceDTO.Item item = new InvoiceDTO.Item();
                        item.name = itemMap.get("Description") != null ? itemMap.get("Description").getValueAsString() : "";
                        item.quantity = itemMap.get("Quantity") != null ? ((Double) itemMap.get("Quantity").getValueAsDouble()).intValue() : 1;
                        item.unitPrice = itemMap.get("UnitPrice") != null ? itemMap.get("UnitPrice").getValueAsCurrency().getAmount() : 0.0;
                        dto.items.add(item);
                    }
                }
            });

            return dto;

        } catch (Exception e) {
            throw new RuntimeException("OCR failed: " + e.getMessage(), e);
        }
    }
}
