package com.kp.invoice.service;


import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LLMService {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${openai.model:gpt-4}")
    private String model;

    public String analyzeDocument(String docContent) {

        OpenAiService service = new OpenAiService(openAiApiKey, Duration.ofSeconds(60));

        String prompt = """
        You are a financial document parser. Analyze the document content below and extract all relevant fields.
        Return the result as a valid JSON object with the following rules:
        1. First field must be "documentType" (e.g., "Bank Statement", "Invoice", "Receipt", etc.)
        2. Extract all key financial fields such as account numbers, dates, balances, transactions, etc.
        3. For transaction lists, use JSON arrays of objects.
        4. Normalize amounts as numeric values (no $ signs).
        5. Use camelCase for all field names.
        6. Return ONLY the JSON object, no explanation or markdown code blocks.
        
        Document Content:
        """ + docContent + "\n";


        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(model)
                .messages(List.of(
                        new ChatMessage("system", "You are a financial document parser. Always return complete, valid JSON only."),
                        new ChatMessage("user", prompt)
                ))
                .temperature(0.2)
                .maxTokens(4000)  // Increased from 1000 to 4000
                .build();

        ChatCompletionResult result = service.createChatCompletion(request);
        String response = result.getChoices().get(0).getMessage().getContent();

        return response;
    }

//    private Map<String, String> parseLlmResponse(String response) {
//        Map<String, String> matches = new HashMap<>();
//        String[] lines = response.split("\n");
//
//        for (String line : lines) {
//            if (line.contains(":")) {
//                String[] parts = line.split(":");
//                if (parts.length == 2) {
//                    matches.put(parts[0].trim(), parts[1].trim());
//                }
//            }
//        }
//
//        return matches;
//    }


}