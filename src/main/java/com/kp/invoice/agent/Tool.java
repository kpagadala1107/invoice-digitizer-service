package com.kp.invoice.agent;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Represents a tool that can be used by the AI agent
 */
public class Tool {

    @JsonProperty("type")
    private String type = "function";

    @JsonProperty("function")
    private FunctionDefinition function;

    public Tool(String name, String description, Map<String, Object> parameters) {
        this.function = new FunctionDefinition(name, description, parameters);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public FunctionDefinition getFunction() {
        return function;
    }

    public void setFunction(FunctionDefinition function) {
        this.function = function;
    }

    public static class FunctionDefinition {
        @JsonProperty("name")
        private String name;

        @JsonProperty("description")
        private String description;

        @JsonProperty("parameters")
        private Map<String, Object> parameters;

        public FunctionDefinition(String name, String description, Map<String, Object> parameters) {
            this.name = name;
            this.description = description;
            this.parameters = parameters;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Map<String, Object> getParameters() {
            return parameters;
        }

        public void setParameters(Map<String, Object> parameters) {
            this.parameters = parameters;
        }
    }
}

