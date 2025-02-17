package org.engteachbot.service;

import org.json.JSONArray;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RandomWordClient {

    private final RestTemplate restTemplate;
    private static final String RANDOM_WORD_API = "https://random-word-api.herokuapp.com/word?number=1";

    public RandomWordClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String fetchRandomWord() {
        try {
            String response = restTemplate.getForObject(RANDOM_WORD_API, String.class);
            JSONArray jsonArray = new JSONArray(response);
            return jsonArray.getString(0);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении случайного слова", e);
        }
    }
}
