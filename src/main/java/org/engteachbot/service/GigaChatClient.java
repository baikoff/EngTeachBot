package org.engteachbot.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GigaChatClient {

    private static final String GIGA_CHAT_API_URL = "https://gigachat.devices.sberbank.ru/api/v1/chat/completions";
    private final RestTemplate restTemplate;
    private final GigaChatAuthService authService;

    public GigaChatClient(RestTemplate restTemplate, GigaChatAuthService authService) {
        this.restTemplate = restTemplate;
        this.authService = authService;
    }

    public String askGigaChat(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");
        headers.setBearerAuth(authService.getAccessToken());

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "GigaChat");
        requestBody.put("messages", new JSONArray().put(new JSONObject().put("role", "user").put("content", prompt)));

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody.toString(), headers);

        ResponseEntity<String> response = restTemplate.exchange(
                GIGA_CHAT_API_URL, HttpMethod.POST, requestEntity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            JSONObject jsonResponse = new JSONObject(response.getBody());
            JSONArray choices = jsonResponse.getJSONArray("choices");
            return choices.getJSONObject(0).getJSONObject("message").getString("content");
        } else {
            return "Ошибка общения с GigaChat: " + response.getBody();
        }
    }
}


