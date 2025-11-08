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
                Analyze and extract the fields from the document content and return as key-value pairs in JSON format. Also include documentType as the first field in the Json.
                Document Content:
                """ +
                docContent + "\n";


        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(model)
                .messages(List.of(new ChatMessage("user", prompt)))
                .temperature(0.2)
                .maxTokens(1000)
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