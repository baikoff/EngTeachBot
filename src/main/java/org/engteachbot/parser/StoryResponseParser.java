package org.engteachbot.parser;

import org.engteachbot.model.WordInfo;
import org.engteachbot.response.StoryResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class StoryResponseParser {

    public StoryResponse parse(String sceneResponse, String wordsResponse) {
        List<WordInfo> words = parseWords(wordsResponse);
        String sceneText = sceneResponse.trim();
        return new StoryResponse(sceneText, words);
    }

    private List<WordInfo> parseWords(String wordsResponse) {
        List<WordInfo> words = new ArrayList<>();
        if (wordsResponse == null || wordsResponse.trim().isEmpty()) {
            System.out.println("Words response is empty");
            return words;
        }

        try {
            // Убираем возможные лишние символы и парсим как JSONArray
            String cleanedResponse = wordsResponse.trim();
            if (!cleanedResponse.startsWith("[")) {
                cleanedResponse = cleanedResponse.substring(cleanedResponse.indexOf("["));
            }
            if (!cleanedResponse.endsWith("]")) {
                cleanedResponse = cleanedResponse.substring(0, cleanedResponse.lastIndexOf("]") + 1);
            }

            JSONArray jsonArray = new JSONArray(cleanedResponse);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonWord = jsonArray.getJSONObject(i);
                String word = jsonWord.getString("word");
                String phonetic = jsonWord.getString("ipa");
                String translatedWord = jsonWord.getString("translation");
                String example = jsonWord.optString("example", "No example provided.");
                String translatedExample = jsonWord.optString("translatedExample", "Пример не переведен.");
                words.add(new WordInfo(word, translatedWord, phonetic, example, translatedExample));
            }
        } catch (Exception e) {
            System.out.println("Failed to parse words response as JSON: " + wordsResponse);
            e.printStackTrace();
        }

        if (words.isEmpty()) {
            System.out.println("No words parsed from words response: " + wordsResponse);
        } else {
            System.out.println("Parsed " + words.size() + " words from response");
        }

        return words;
    }
}
