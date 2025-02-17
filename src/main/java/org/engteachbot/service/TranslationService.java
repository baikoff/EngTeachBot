package org.engteachbot.service;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class TranslationService {

    private final RestTemplate restTemplate;

    public TranslationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String translate(String text, String sourceLang, String targetLang) {
        String apiUrl = "http://127.0.0.1:5000/translate";

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("q", text);
        requestBody.put("source", sourceLang);
        requestBody.put("target", targetLang);

        try {
            String response = restTemplate.postForObject(apiUrl, requestBody, String.class);
            JSONObject jsonResponse = new JSONObject(response);
            return jsonResponse.getString("translatedText");

        } catch (Exception e) {
            return "Ошибка перевода: " + e.getMessage();
        }
    }

}
