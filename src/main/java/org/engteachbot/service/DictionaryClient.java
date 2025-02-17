package org.engteachbot.service;

import org.engteachbot.model.WordInfo;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DictionaryClient {

    private final RestTemplate restTemplate;
    private static final String DICTIONARY_API = "https://api.dictionaryapi.dev/api/v2/entries/en/";

    public DictionaryClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public WordInfo fetchWordInfo(String word) {
        try {
            String response = restTemplate.getForObject(DICTIONARY_API + word, String.class);
            JSONArray jsonArray = new JSONArray(response);
            JSONObject jsonWord = jsonArray.getJSONObject(0);

            String phonetic = jsonWord.optString("phonetic", "Нет транскрипции");
            String definition = jsonWord.getJSONArray("meanings")
                    .getJSONObject(0)
                    .getJSONArray("definitions")
                    .getJSONObject(0)
                    .getString("definition");
            String example = jsonWord.getJSONArray("meanings")
                    .getJSONObject(0)
                    .getJSONArray("definitions")
                    .getJSONObject(0)
                    .optString("example", "Нет примера");

            return new WordInfo(word, phonetic, definition, example, null, null, null);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении информации о слове", e);
        }
    }
}
