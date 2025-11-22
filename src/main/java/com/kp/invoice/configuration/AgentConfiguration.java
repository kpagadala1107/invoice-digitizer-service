package com.kp.invoice.configuration;

import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for AI Agent components
 */
@Configuration
public class AgentConfiguration {

    @Value("${azure.api.endpoint}")
    private String azureEndpoint;

    @Value("${azure.api.key}")
    private String azureApiKey;

    /**
     * DocumentAnalysisClient bean for Azure Document Intelligence
     */
    @Bean
    public DocumentAnalysisClient documentAnalysisClient() {
        return new DocumentAnalysisClientBuilder()
            .credential(new AzureKeyCredential(azureApiKey))
            .endpoint(azureEndpoint)
            .buildClient();
    }

    /**
     * ObjectMapper bean for JSON processing
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}

