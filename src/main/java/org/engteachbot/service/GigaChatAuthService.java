package org.engteachbot.service;

import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class GigaChatAuthService {

    private static final String AUTH_URL = "https://ngw.devices.sberbank.ru:9443/api/v2/oauth";
    private final RestTemplate restTemplate;
    private final String authKey;

    public GigaChatAuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.authKey = System.getenv("GIGA_CHAT_AUTH_KEY");
        if (authKey == null || authKey.isEmpty()) {
            throw new IllegalStateException("GIGA_CHAT_AUTH_KEY не задан!");
        }
    }

    public String getAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + authKey);
        headers.set("Accept", "application/json");
        headers.set("RqUID", UUID.randomUUID().toString());

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("scope", "GIGACHAT_API_PERS");
        requestBody.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(AUTH_URL, HttpMethod.POST, requestEntity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            JSONObject jsonResponse = new JSONObject(response.getBody());
            return jsonResponse.getString("access_token");
        } else {
            throw new RuntimeException("Ошибка получения токена: " + response.getBody());
        }
    }
}

