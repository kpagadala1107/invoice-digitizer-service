package com.kp.invoice.agent.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kp.invoice.model.Invoice;
import com.kp.invoice.repository.InvoiceRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tool for retrieving records from database
 */
@Component
public class DatabaseRetrievalTool implements AgentTool {

    private final InvoiceRepository invoiceRepository;
    private final ObjectMapper objectMapper;

    public DatabaseRetrievalTool(InvoiceRepository invoiceRepository, ObjectMapper objectMapper) {
        this.invoiceRepository = invoiceRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "retrieve_database_record";
    }

    @Override
    public String getDescription() {
        return "Retrieves existing records from the database for businesses or individuals. " +
               "Can search by invoice number, vendor name, ID, or retrieve all records. " +
               "Returns matching invoice records with full details including items and amounts.";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();

        Map<String, Object> searchType = new HashMap<>();
        searchType.put("type", "string");
        searchType.put("description", "Type of search: 'id', 'vendor', 'invoiceNumber', or 'all'");
        searchType.put("enum", new String[]{"id", "vendor", "invoiceNumber", "all"});
        properties.put("searchType", searchType);

        Map<String, Object> searchValue = new HashMap<>();
        searchValue.put("type", "string");
        searchValue.put("description", "The value to search for (not required for 'all' search type)");
        properties.put("searchValue", searchValue);

        schema.put("properties", properties);
        schema.put("required", new String[]{"searchType"});

        return schema;
    }

    @Override
    public String execute(Map<String, Object> parameters) {
        try {
            String searchType = (String) parameters.get("searchType");
            String searchValue = (String) parameters.get("searchValue");

            List<Invoice> results;

            switch (searchType) {
                case "id":
                    if (searchValue == null) {
                        return createErrorResponse("searchValue is required for id search");
                    }
                    Invoice invoice = invoiceRepository.findById(searchValue).orElse(null);
                    results = invoice != null ? List.of(invoice) : List.of();
                    break;

                case "vendor":
                    if (searchValue == null) {
                        return createErrorResponse("searchValue is required for vendor search");
                    }
                    results = invoiceRepository.findAll().stream()
                        .filter(inv -> inv.getVendor() != null &&
                               inv.getVendor().toLowerCase().contains(searchValue.toLowerCase()))
                        .toList();
                    break;

                case "invoiceNumber":
                    if (searchValue == null) {
                        return createErrorResponse("searchValue is required for invoiceNumber search");
                    }
                    results = invoiceRepository.findAll().stream()
                        .filter(inv -> inv.getInvoiceNumber() != null &&
                               inv.getInvoiceNumber().equalsIgnoreCase(searchValue))
                        .toList();
                    break;

                case "all":
                    results = invoiceRepository.findAll();
                    break;

                default:
                    return createErrorResponse("Invalid searchType. Use 'id', 'vendor', 'invoiceNumber', or 'all'");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("count", results.size());
            response.put("results", results);

            return objectMapper.writeValueAsString(response);

        } catch (Exception e) {
            return createErrorResponse("Failed to retrieve database records: " + e.getMessage());
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

