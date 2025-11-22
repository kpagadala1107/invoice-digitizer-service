package com.kp.invoice.agent.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Tool for validating document data
 */
@Component
public class DocumentValidationTool implements AgentTool {

    private final ObjectMapper objectMapper;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[(]?[0-9]{1,4}[)]?[-\\s.]?[(]?[0-9]{1,4}[)]?[-\\s.]?[0-9]{1,9}$");

    public DocumentValidationTool(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String getName() {
        return "validate_document";
    }

    @Override
    public String getDescription() {
        return "Validates document data for completeness, correctness, and business rules compliance. " +
               "Checks for required fields, data format validity, calculation accuracy, " +
               "date validity, amount consistency, and other business rules. " +
               "Returns validation status with detailed error messages if validation fails.";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");

        Map<String, Object> properties = new HashMap<>();

        Map<String, Object> documentData = new HashMap<>();
        documentData.put("type", "string");
        documentData.put("description", "The document data to validate (as JSON string)");
        properties.put("documentData", documentData);

        Map<String, Object> validationType = new HashMap<>();
        validationType.put("type", "string");
        validationType.put("description", "Type of validation: 'invoice', 'receipt', 'general'");
        validationType.put("default", "general");
        properties.put("validationType", validationType);

        schema.put("properties", properties);
        schema.put("required", new String[]{"documentData"});

        return schema;
    }

    @Override
    public String execute(Map<String, Object> parameters) {
        try {
            String documentDataStr = (String) parameters.get("documentData");
            String validationType = (String) parameters.getOrDefault("validationType", "general");

            Map<String, Object> documentData;
            try {
                documentData = objectMapper.readValue(documentDataStr, Map.class);
            } catch (Exception e) {
                return createValidationResponse(false, List.of("Invalid JSON format for document data"));
            }

            List<String> errors = new ArrayList<>();
            List<String> warnings = new ArrayList<>();

            switch (validationType) {
                case "invoice":
                    validateInvoice(documentData, errors, warnings);
                    break;
                case "receipt":
                    validateReceipt(documentData, errors, warnings);
                    break;
                case "general":
                default:
                    validateGeneral(documentData, errors, warnings);
                    break;
            }

            boolean isValid = errors.isEmpty();

            Map<String, Object> response = new HashMap<>();
            response.put("isValid", isValid);
            response.put("errors", errors);
            response.put("warnings", warnings);
            response.put("validationType", validationType);
            response.put("timestamp", LocalDate.now().toString());

            return objectMapper.writeValueAsString(response);

        } catch (Exception e) {
            return createErrorResponse("Failed to validate document: " + e.getMessage());
        }
    }

    private void validateInvoice(Map<String, Object> data, List<String> errors, List<String> warnings) {
        // Required fields validation
        validateRequiredField(data, "invoiceNumber", errors);
        validateRequiredField(data, "vendor", errors);
        validateRequiredField(data, "date", errors);
        validateRequiredField(data, "totalAmount", errors);

        // Date validation
        if (data.containsKey("date")) {
            validateDate(data.get("date").toString(), errors, warnings);
        }

        // Amount validation
        if (data.containsKey("totalAmount")) {
            validateAmount(data.get("totalAmount"), "totalAmount", errors);
        }

        if (data.containsKey("tax")) {
            validateAmount(data.get("tax"), "tax", errors);
        }

        // Items validation
        if (data.containsKey("items")) {
            validateItems(data.get("items"), errors, warnings);
        }

        // Calculate totals
        if (data.containsKey("items") && data.containsKey("totalAmount")) {
            validateTotalCalculation(data, errors, warnings);
        }

        // Vendor validation
        if (data.containsKey("vendor")) {
            String vendor = data.get("vendor").toString();
            if (vendor.trim().length() < 2) {
                errors.add("Vendor name is too short");
            }
        }

        // Email validation if present
        if (data.containsKey("email")) {
            validateEmail(data.get("email").toString(), errors);
        }

        // Phone validation if present
        if (data.containsKey("phone")) {
            validatePhone(data.get("phone").toString(), warnings);
        }
    }

    private void validateReceipt(Map<String, Object> data, List<String> errors, List<String> warnings) {
        validateRequiredField(data, "date", errors);
        validateRequiredField(data, "totalAmount", errors);

        if (data.containsKey("date")) {
            validateDate(data.get("date").toString(), errors, warnings);
        }

        if (data.containsKey("totalAmount")) {
            validateAmount(data.get("totalAmount"), "totalAmount", errors);
        }
    }

    private void validateGeneral(Map<String, Object> data, List<String> errors, List<String> warnings) {
        if (data.isEmpty()) {
            errors.add("Document data is empty");
        }

        // Check for common fields
        data.forEach((key, value) -> {
            if (value == null) {
                warnings.add("Field '" + key + "' is null");
            } else if (value.toString().trim().isEmpty()) {
                warnings.add("Field '" + key + "' is empty");
            }
        });
    }

    private void validateRequiredField(Map<String, Object> data, String fieldName, List<String> errors) {
        if (!data.containsKey(fieldName) || data.get(fieldName) == null ||
            data.get(fieldName).toString().trim().isEmpty()) {
            errors.add("Required field '" + fieldName + "' is missing or empty");
        }
    }

    private void validateDate(String dateStr, List<String> errors, List<String> warnings) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            LocalDate now = LocalDate.now();

            if (date.isAfter(now)) {
                warnings.add("Date is in the future: " + dateStr);
            }

            if (date.isBefore(now.minusYears(10))) {
                warnings.add("Date is more than 10 years old: " + dateStr);
            }
        } catch (Exception e) {
            errors.add("Invalid date format: " + dateStr);
        }
    }

    private void validateAmount(Object amount, String fieldName, List<String> errors) {
        try {
            double value = Double.parseDouble(amount.toString());
            if (value < 0) {
                errors.add(fieldName + " cannot be negative");
            }
            if (value == 0) {
                errors.add(fieldName + " cannot be zero");
            }
        } catch (Exception e) {
            errors.add("Invalid amount format for " + fieldName + ": " + amount);
        }
    }

    private void validateItems(Object items, List<String> errors, List<String> warnings) {
        if (!(items instanceof List)) {
            errors.add("Items must be a list");
            return;
        }

        List<?> itemList = (List<?>) items;
        if (itemList.isEmpty()) {
            warnings.add("Invoice has no items");
        }

        for (int i = 0; i < itemList.size(); i++) {
            if (!(itemList.get(i) instanceof Map)) {
                errors.add("Item " + i + " is not a valid object");
                continue;
            }

            Map<String, Object> item = (Map<String, Object>) itemList.get(i);

            if (!item.containsKey("quantity") || !item.containsKey("unitPrice")) {
                errors.add("Item " + i + " is missing quantity or unitPrice");
            }
        }
    }

    private void validateTotalCalculation(Map<String, Object> data, List<String> errors, List<String> warnings) {
        try {
            double totalAmount = Double.parseDouble(data.get("totalAmount").toString());
            double calculatedTotal = 0.0;

            if (data.get("items") instanceof List) {
                List<?> items = (List<?>) data.get("items");
                for (Object item : items) {
                    if (item instanceof Map) {
                        Map<String, Object> itemMap = (Map<String, Object>) item;
                        if (itemMap.containsKey("quantity") && itemMap.containsKey("unitPrice")) {
                            double qty = Double.parseDouble(itemMap.get("quantity").toString());
                            double price = Double.parseDouble(itemMap.get("unitPrice").toString());
                            calculatedTotal += qty * price;
                        }
                    }
                }

                // Add tax if present
                if (data.containsKey("tax")) {
                    calculatedTotal += Double.parseDouble(data.get("tax").toString());
                }

                double difference = Math.abs(totalAmount - calculatedTotal);
                if (difference > 0.01) { // Allow for rounding errors
                    errors.add(String.format("Total amount mismatch. Expected: %.2f, Calculated: %.2f",
                        totalAmount, calculatedTotal));
                }
            }
        } catch (Exception e) {
            warnings.add("Could not validate total calculation: " + e.getMessage());
        }
    }

    private void validateEmail(String email, List<String> errors) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            errors.add("Invalid email format: " + email);
        }
    }

    private void validatePhone(String phone, List<String> warnings) {
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            warnings.add("Phone number format may be invalid: " + phone);
        }
    }

    private String createValidationResponse(boolean isValid, List<String> errors) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("isValid", isValid);
            response.put("errors", errors);
            response.put("warnings", new ArrayList<>());
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return "{\"isValid\": false, \"errors\": [\"" + String.join("\", \"", errors) + "\"]}";
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

