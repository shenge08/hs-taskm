package com.taskm.cli.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskm.entity.Strategy;
import com.taskm.entity.Task;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * REST API client for communicating with HS-TASKM backend.
 */
@Component
public class ApiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public ApiClient(
            @Value("${api.base-url:http://localhost:8080}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Get all strategies, optionally filtered by language.
     */
    public List<Strategy> getStrategies(String language) {
        String url = baseUrl + "/api/strategies";
        if (language != null && !language.isEmpty()) {
            url += "?language=" + language;
        }

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                // Parse response: {"code":200,"message":"success","data":[...]}
                var jsonNode = objectMapper.readTree(response.getBody());
                var dataNode = jsonNode.get("data");
                return objectMapper.convertValue(dataNode, new TypeReference<List<Strategy>>() {});
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse response: " + e.getMessage(), e);
            }
        } else {
            throw new RuntimeException("API call failed with status: " + response.getStatusCode());
        }
    }

    /**
     * Get strategy by ID.
     */
    public Strategy getStrategy(Long id) {
        String url = baseUrl + "/api/strategies/" + id;

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                var jsonNode = objectMapper.readTree(response.getBody());
                var dataNode = jsonNode.get("data");
                return objectMapper.convertValue(dataNode, Strategy.class);
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse response: " + e.getMessage(), e);
            }
        } else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            throw new RuntimeException("Strategy not found with id: " + id);
        } else {
            throw new RuntimeException("API call failed with status: " + response.getStatusCode());
        }
    }

    /**
     * Get all tasks, optionally filtered by status.
     */
    public List<Task> getTasks(String status, int page, int size) {
        String url = baseUrl + "/api/tasks?page=" + page + "&size=" + size;
        if (status != null && !status.isEmpty()) {
            url += "&status=" + status;
        }

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                var jsonNode = objectMapper.readTree(response.getBody());
                var dataNode = jsonNode.get("data");
                var recordsNode = dataNode.get("records");
                return objectMapper.convertValue(recordsNode, new TypeReference<List<Task>>() {});
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse response: " + e.getMessage(), e);
            }
        } else {
            throw new RuntimeException("API call failed with status: " + response.getStatusCode());
        }
    }

    /**
     * Get task by ID.
     */
    public Task getTask(Long id) {
        String url = baseUrl + "/api/tasks/" + id;

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                var jsonNode = objectMapper.readTree(response.getBody());
                var dataNode = jsonNode.get("data");
                return objectMapper.convertValue(dataNode, Task.class);
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse response: " + e.getMessage(), e);
            }
        } else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            throw new RuntimeException("Task not found with id: " + id);
        } else {
            throw new RuntimeException("API call failed with status: " + response.getStatusCode());
        }
    }
}
