package com.scripfinder.module1.Schedulers;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scripfinder.module1.config.UpstoxProperties;

@Component
@EnableAsync
public class ScheduledGetAuthorizationToken {

    private static final String apiUrl = "https://api-v2.upstox.com/v2/login/authorization/token";
    
    private UpstoxProperties upstoxProperties;
    
    @Autowired
    public ScheduledGetAuthorizationToken(UpstoxProperties upstoxProperties) {
        this.upstoxProperties = upstoxProperties;
    }


    @PostConstruct
    protected void onStartup() {
        run();
    }
    @Async
    @Scheduled(cron = "0 30 3 * * ?",zone = "Asia/Kolkata") // Everyday at 3:30 AM
    protected void dailyTask() {
        run();
    }

    private void run() {
        try {
            String accessToken = getAccessToken();
            if(accessToken != null){
                upstoxProperties.setAccessToken(accessToken);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private String getAccessToken() {
        // Set up the request body
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("code", upstoxProperties.getAccessToken());
        requestBody.add("client_id", upstoxProperties.getApiKey());
        requestBody.add("client_secret", upstoxProperties.getSecretKey());
        requestBody.add("redirect_uri", upstoxProperties.getRedirectUrl());
        requestBody.add("grant_type", "authorization_code");

        // Set up the headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("accept", "application/json");
        headers.set("Api-Version", "2.0");

        // Set up the request entity
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        // Create a RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Make the POST request
        ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, String.class);

        // Parse JSON response
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(responseEntity.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        // Extract access_token
        String accessToken = jsonNode.get("access_token").asText();

        return accessToken;
    }
}
