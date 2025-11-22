package com.kp.invoice.agent.tools;

import java.util.Map;

/**
 * Interface for all AI Agent tools
 */
public interface AgentTool {

    /**
     * Get the name of the tool
     */
    String getName();

    /**
     * Get the description of what the tool does
     */
    String getDescription();

    /**
     * Get the JSON schema for the tool's parameters
     */
    Map<String, Object> getParametersSchema();

    /**
     * Execute the tool with given parameters
     * @param parameters Map of parameter name to value
     * @return Result as a JSON string
     */
    String execute(Map<String, Object> parameters);
}

