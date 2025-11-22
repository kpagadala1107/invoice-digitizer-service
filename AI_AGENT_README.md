# Doc Digitizer AI Agent

An intelligent AI agent specialized in document processing and analysis, powered by OpenAI GPT and Azure Document Intelligence.

## Agent Description

**Doc Digitizer** is an AI-powered agent that can understand natural language requests and automatically orchestrate complex document processing workflows. The agent uses function calling to execute specialized tools and provide intelligent responses.

## Core Capabilities

### 1. **Extract Raw Data from Azure Document Intelligence**
- Supports multiple document types: invoices, receipts, identity documents, and custom forms
- Returns structured field-value pairs with confidence scores
- Extracts text, tables, and key-value pairs automatically

### 2. **Convert to JSON Format**
- Converts extracted document data to properly formatted JSON
- Handles raw text, key-value pairs, or structured data
- Supports pretty printing for readability

### 3. **Convert to XML Format**
- Transforms document data to XML format
- Configurable root element names
- Useful for legacy system integration

### 4. **Retrieve Records from Database**
- Search existing invoices by ID, vendor name, or invoice number
- Retrieve all records or filtered results
- Returns complete invoice details including items and amounts

### 5. **Validate Documents**
- Comprehensive validation for invoices and receipts
- Checks required fields, data formats, and business rules
- Validates calculations, dates, amounts, and totals
- Returns detailed error and warning messages

## API Endpoints

### Agent Information
```http
GET /api/agent/info
```
Returns agent name, description, version, and all available capabilities.

### List Tools
```http
GET /api/agent/tools
```
Lists all available tools with their descriptions and parameter schemas.

### Chat with Agent
```http
POST /api/agent/chat
Content-Type: application/json

{
  "message": "Extract data from my invoice and validate it"
}
```

### Process Document
```http
POST /api/agent/process-document
Content-Type: multipart/form-data

file: [document file]
instruction: "Extract and validate this invoice"
```

### Validate Document
```http
POST /api/agent/validate
Content-Type: application/json

{
  "documentData": "{\"invoiceNumber\": \"INV-001\", ...}",
  "validationType": "invoice"
}
```

### Search Database
```http
GET /api/agent/search?searchType=vendor&searchValue=Acme%20Corp
```

## Usage Examples

### Example 1: Extract and Validate Invoice
```bash
curl -X POST http://localhost:8081/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "I need to extract data from an invoice and validate it"
  }'
```

### Example 2: Process Document File
```bash
curl -X POST http://localhost:8081/api/agent/process-document \
  -F "file=@invoice.pdf" \
  -F "instruction=Extract data, convert to JSON, and validate"
```

### Example 3: Search for Existing Invoices
```bash
curl -X POST http://localhost:8081/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Find all invoices from vendor ABC Company"
  }'
```

### Example 4: Convert Data Format
```bash
curl -X POST http://localhost:8081/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Convert this invoice data to XML format: {\"invoiceNumber\": \"INV-001\", \"vendor\": \"Acme Corp\", \"total\": 1500.00}"
  }'
```

## Tool Details

### 1. extract_raw_data_azure
**Purpose**: Extract structured data from documents using Azure Document Intelligence

**Parameters**:
- `fileData` (required): Base64 encoded file data or file path
- `modelType` (optional): Model type (default: "prebuilt-document")
  - Options: prebuilt-invoice, prebuilt-receipt, prebuilt-document

**Returns**: JSON with extracted fields, confidence scores, and document type

### 2. convert_to_json
**Purpose**: Convert document data to properly formatted JSON

**Parameters**:
- `data` (required): Data to convert (raw text, key-value pairs, or structured data)
- `prettyPrint` (optional): Whether to pretty print (default: true)

**Returns**: Formatted JSON string

### 3. convert_to_xml
**Purpose**: Convert document data to XML format

**Parameters**:
- `data` (required): Data to convert
- `rootElement` (optional): Root XML element name (default: "document")

**Returns**: Formatted XML string

### 4. retrieve_database_record
**Purpose**: Retrieve existing records from the database

**Parameters**:
- `searchType` (required): Type of search
  - Options: "id", "vendor", "invoiceNumber", "all"
- `searchValue` (optional): Value to search for (not required for "all")

**Returns**: JSON with matching invoice records

### 5. validate_document
**Purpose**: Validate document data for correctness and completeness

**Parameters**:
- `documentData` (required): Document data as JSON string
- `validationType` (optional): Type of validation (default: "general")
  - Options: "invoice", "receipt", "general"

**Returns**: Validation result with errors and warnings

## Architecture

### Components

1. **DocDigitizerAgentService**: Main orchestration service
   - Manages conversation with OpenAI
   - Routes function calls to appropriate tools
   - Handles context and state

2. **AgentTool Interface**: Common interface for all tools
   - Defines standard methods for tool execution
   - Provides parameter schemas for function calling

3. **Tool Implementations**: Specialized tools for each capability
   - AzureDocumentIntelligenceTool
   - JsonConverterTool
   - XmlConverterTool
   - DatabaseRetrievalTool
   - DocumentValidationTool

4. **AgentController**: REST API endpoints for agent interaction

### Technology Stack

- **Spring Boot 3.5.6**: Application framework
- **OpenAI GPT-4/GPT-4o-mini**: Language model for agent intelligence
- **Azure Document Intelligence**: OCR and document analysis
- **MongoDB**: Database for invoice storage
- **Jackson**: JSON/XML processing

## Configuration

Add these properties to `application.properties`:

```properties
# OpenAI Configuration
openai.api.key=your-openai-api-key
openai.model=gpt-4o-mini

# Azure Document Intelligence
azure.api.endpoint=https://your-resource.cognitiveservices.azure.com/
azure.api.key=your-azure-key

# MongoDB
spring.data.mongodb.uri=your-mongodb-connection-string

# Server
server.port=8081
```

## Advanced Features

### Multi-Step Workflows
The agent can chain multiple tools together automatically:
```
User: "Extract data from this invoice, convert it to XML, and validate it"
Agent: 
  1. Calls extract_raw_data_azure
  2. Calls convert_to_xml with extracted data
  3. Calls validate_document
  4. Returns comprehensive results
```

### Context Awareness
The agent maintains context across the conversation and can reference previous operations.

### Error Handling
Each tool includes robust error handling with informative error messages.

### Validation Rules
- Required field checks
- Date format and range validation
- Amount calculations and consistency
- Email and phone format validation
- Business rule compliance

## Future Enhancements

### Potential Additional Capabilities:
1. **Batch Processing**: Process multiple documents at once
2. **Custom Rules Engine**: User-defined validation rules
3. **OCR Quality Assessment**: Evaluate extraction confidence
4. **Duplicate Detection**: Identify duplicate invoices
5. **Workflow Automation**: Trigger actions based on document type
6. **Export to Multiple Formats**: CSV, Excel, PDF reports
7. **Historical Analysis**: Trend analysis and insights
8. **Smart Routing**: Automatic routing to appropriate workflows
9. **Multi-Language Support**: Process documents in multiple languages
10. **Integration Tools**: Connect to ERP, accounting systems, etc.

## Development

### Build and Run
```bash
# Build
mvn clean install

# Run
mvn spring-boot:run

# Or run the JAR
java -jar target/invoice-digitizer-service-0.0.1-SNAPSHOT.jar
```

### Testing
```bash
# Run tests
mvn test

# Test agent endpoint
curl http://localhost:8081/api/agent/info
```

## License

Copyright (c) 2025. All rights reserved.

