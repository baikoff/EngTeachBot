package org.engteachbot.service;

import org.engteachbot.model.WordInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
public class GigaChatWordOfTheDayService implements WordOfTheDayService {

    private final GigaChatClient gigaChatClient;
    private final List<String> commonWords = Arrays.asList("love", "friend", "work", "home", "time");

    @Autowired
    public GigaChatWordOfTheDayService(GigaChatClient gigaChatClient) {
        this.gigaChatClient = gigaChatClient;
    }

    @Override
    public WordInfo getWordOfTheDay() {
        String word = commonWords.get(new Random().nextInt(commonWords.size()));
        String prompt = String.format(
                "Дай информацию о слове '%s' на английском для изучения языка. Укажи: перевод на русский, транскрипцию (IPA), " +
                        "определение на английском, перевод определения на русский, пример использования на английском и перевод примера на русский.",
                word
        );
        String response = gigaChatClient.askGigaChat(prompt);
        System.out.println("Word of the Day Response: " + response);
        return parseResponseToWordInfo(word, response);
    }

    private WordInfo parseResponseToWordInfo(String word, String response) {
        try {
            String[] lines = response.split("\n");
            String translatedWord = "";
            String phonetic = "";
            String definition = "";
            String translatedDefinition = "";
            String example = "";
            String translatedExample = "";

            boolean exampleNext = false;
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.contains("(сущ.)")) {
                    translatedWord = line.split("\\(сущ\\.\\)")[1].trim();
                } else if (line.contains("Translation:")) {
                    translatedWord = line.split("Translation:")[1].trim();
                } else if (line.startsWith("[ˈ")) {
                    phonetic = line.trim();
                } else if (line.contains("Pronunciation:")) {
                    phonetic = line.split("Pronunciation:")[1].trim();
                } else if (line.contains("Definition:")) {
                    definition = line.split("Definition:")[1].trim();
                } else if (line.contains("Translation of definition:")) {
                    translatedDefinition = line.split("Translation of definition:")[1].trim();
                } else if (line.contains("Example:")) {
                    exampleNext = true; // Следующая строка — пример
                } else if (exampleNext && line.startsWith("*")) {
                    example = line.replace("*", "").trim();
                    exampleNext = false;
                } else if (line.contains("Перевод:") && !translatedExample.isEmpty()) {
                    translatedExample = line.split("Перевод:")[1].replace("*", "").trim();
                }
            }
            if (translatedWord.isEmpty()) translatedWord = "не определено";
            if (phonetic.isEmpty()) phonetic = "не определено";
            if (definition.isEmpty()) definition = "не определено";
            if (translatedDefinition.isEmpty()) translatedDefinition = "не определено";
            if (example.isEmpty()) example = "не определено";
            if (translatedExample.isEmpty()) translatedExample = "не определено";

            return new WordInfo(word, translatedWord, phonetic, definition, translatedDefinition, example, translatedExample);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

