# Quick Start Guide - Doc Digitizer AI Agent

## Getting Started

### 1. Start the Application

```bash
cd /Users/kiranpagadala/IdeaProjects/invoice-digitizer-service
./mvnw spring-boot:run
```

The service will start on `http://localhost:8081`

### 2. Verify Agent is Running

```bash
curl http://localhost:8081/api/agent/info | json_pp
```

**Expected Response:**
```json
{
  "name": "Doc Digitizer",
  "description": "An intelligent AI agent specialized in document processing and analysis",
  "version": "1.0.0",
  "capabilities": [
    {
      "name": "extract_raw_data_azure",
      "description": "Extracts raw data from documents using Azure Document Intelligence..."
    },
    {
      "name": "convert_to_json",
      "description": "Converts document data to properly formatted JSON..."
    },
    {
      "name": "convert_to_xml",
      "description": "Converts document data to properly formatted XML..."
    },
    {
      "name": "retrieve_database_record",
      "description": "Retrieves existing records from the database..."
    },
    {
      "name": "validate_document",
      "description": "Validates document data for completeness and accuracy..."
    }
  ]
}
```

### 3. List All Available Tools

```bash
curl http://localhost:8081/api/agent/tools | json_pp
```

## Usage Examples

### Example 1: Chat with the Agent

Ask the agent about its capabilities:

```bash
curl -X POST http://localhost:8081/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "What can you help me with?"
  }'
```

### Example 2: Ask About Document Processing

```bash
curl -X POST http://localhost:8081/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "How can I extract data from an invoice?"
  }'
```

### Example 3: Process a Document File

```bash
curl -X POST http://localhost:8081/api/agent/process-document \
  -F "file=@/path/to/invoice.pdf" \
  -F "instruction=Extract all fields from this invoice using Azure"
```

### Example 4: Convert Data to JSON

```bash
curl -X POST http://localhost:8081/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Convert to JSON: InvoiceNumber: INV-001, Vendor: Acme Corp, Total: 1500.00"
  }'
```

### Example 5: Validate Invoice Data

```bash
curl -X POST http://localhost:8081/api/agent/validate \
  -H "Content-Type: application/json" \
  -d '{
    "documentData": "{\"invoiceNumber\": \"INV-001\", \"vendor\": \"Acme Corp\", \"date\": \"2025-11-21\", \"totalAmount\": 1500.00, \"tax\": 150.00, \"items\": [{\"name\": \"Product A\", \"quantity\": 10, \"unitPrice\": 135.00}]}",
    "validationType": "invoice"
  }'
```

**Expected Response:**
```json
{
  "response": "{\"isValid\":true,\"errors\":[],\"warnings\":[],\"validationType\":\"invoice\",\"timestamp\":\"2025-11-21\"}"
}
```

### Example 6: Search Database for Vendor

```bash
curl -X GET "http://localhost:8081/api/agent/search?searchType=vendor&searchValue=Acme%20Corp"
```

### Example 7: Get All Invoices

```bash
curl -X GET "http://localhost:8081/api/agent/search?searchType=all"
```

### Example 8: Convert Data to XML

```bash
curl -X POST http://localhost:8081/api/agent/execute-tool \
  -H "Content-Type: application/json" \
  -d '{
    "toolName": "convert_to_xml",
    "parameters": "{\"data\": \"{\\\"invoiceNumber\\\": \\\"INV-001\\\", \\\"vendor\\\": \\\"Acme Corp\\\", \\\"total\\\": 1500.00}\", \"rootElement\": \"invoice\"}"
  }'
```

## Testing Each Tool Individually

### Tool 1: Extract Raw Data (Azure Document Intelligence)

```bash
# First, upload a document through the existing invoice endpoint
curl -X POST http://localhost:8081/api/invoices/upload \
  -F "file=@/path/to/invoice.pdf"
```

### Tool 2: Convert to JSON

```bash
curl -X POST http://localhost:8081/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "convert to json: InvoiceNumber: INV-123, Vendor: Test Corp, Amount: 500"
  }'
```

### Tool 3: Convert to XML

```bash
curl -X POST http://localhost:8081/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "convert to xml: InvoiceNumber: INV-123, Vendor: Test Corp, Amount: 500"
  }'
```

### Tool 4: Retrieve Database Records

```bash
# Search by vendor
curl -X POST http://localhost:8081/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "search for all invoices"
  }'
```

### Tool 5: Validate Document

```bash
curl -X POST http://localhost:8081/api/agent/validate \
  -H "Content-Type: application/json" \
  -d '{
    "documentData": "{\"invoiceNumber\": \"INV-001\", \"vendor\": \"Acme\", \"date\": \"2025-11-21\", \"totalAmount\": 1500}",
    "validationType": "invoice"
  }'
```

## Common Workflows

### Complete Invoice Processing Workflow

1. **Upload and Extract Data**
```bash
curl -X POST http://localhost:8081/api/agent/process-document \
  -F "file=@invoice.pdf" \
  -F "instruction=Extract data from this invoice"
```

2. **Validate the Extracted Data**
```bash
curl -X POST http://localhost:8081/api/agent/validate \
  -H "Content-Type: application/json" \
  -d '{
    "documentData": "<extracted_data_from_step_1>",
    "validationType": "invoice"
  }'
```

3. **Convert to Desired Format**
```bash
curl -X POST http://localhost:8081/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "convert to xml: <extracted_data>"
  }'
```

4. **Check for Duplicates**
```bash
curl -X GET "http://localhost:8081/api/agent/search?searchType=invoiceNumber&searchValue=INV-001"
```

## Troubleshooting

### Agent not responding
- Check if the service is running: `curl http://localhost:8081/api/agent/info`
- Check logs: `./mvnw spring-boot:run` (look for errors)
- Verify OpenAI API key is set in application.properties

### Tools not executing
- Ensure the request includes proper keywords (extract, convert, validate, search)
- For direct tool execution, include necessary context in the request

### Azure Document Intelligence errors
- Verify Azure credentials in application.properties
- Check file format (PDF, PNG, JPEG supported)
- Ensure file size is under 2MB

### Database retrieval returns empty
- Check MongoDB connection string
- Verify database has data: use existing `/api/invoices` endpoints

## Configuration

Ensure these properties are set in `application.properties`:

```properties
# OpenAI Configuration
openai.api.key=your-openai-key
openai.model=gpt-4o-mini

# Azure Document Intelligence
azure.api.endpoint=your-azure-endpoint
azure.api.key=your-azure-key

# MongoDB
spring.data.mongodb.uri=your-mongodb-uri

# Server
server.port=8081
```

## Next Steps

1. Test each tool individually using the examples above
2. Try combining multiple tools in a workflow
3. Explore the conversational capabilities with the chat endpoint
4. Integrate with your frontend application

## API Documentation

For full API documentation, refer to `AI_AGENT_README.md`

## Support

For issues or questions:
- Check the logs for detailed error messages
- Verify all configuration properties are set correctly
- Ensure all dependencies are installed: `./mvnw clean install`

