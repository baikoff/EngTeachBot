package org.engteachbot.service;

import org.engteachbot.model.WordInfo;
import org.springframework.stereotype.Service;

@Service
public class WordOfTheDayService {

    private final DictionaryClient dictionaryClient;
    private final RandomWordClient randomWordClient;
    private final TranslationService translationService;

    public WordOfTheDayService(DictionaryClient dictionaryClient, RandomWordClient randomWordClient, TranslationService translationService) {
        this.dictionaryClient = dictionaryClient;
        this.randomWordClient = randomWordClient;
        this.translationService = translationService;
    }

    public WordInfo getWordOfTheDay() {
        int maxAttempts = 5;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            String randomWord = randomWordClient.fetchRandomWord();
            if (randomWord != null) {
                WordInfo wordInfo = dictionaryClient.fetchWordInfo(randomWord);
                if (wordInfo != null) {
                    return enrichWordInfo(wordInfo);
                } else {
                    System.out.println("Попытка " + attempt + ": Определение для слова '" + randomWord + "' не найдено.");
                }
            } else {
                System.out.println("Попытка " + attempt + ": Не удалось получить случайное слово.");
            }
        }
        System.out.println("Не удалось получить слово дня после " + maxAttempts + " попыток.");
        return null;
    }

    private WordInfo enrichWordInfo(WordInfo wordInfo) {
        String translatedWord = translationService.translate(wordInfo.getWord(), "en", "ru");
        String translatedDefinition = translationService.translate(wordInfo.getDefinition(), "en", "ru");
        String translatedExample = wordInfo.getExample().equals("Нет примера") ? "Нет примера" :
                translationService.translate(wordInfo.getExample(), "en", "ru");

        return new WordInfo(
                wordInfo.getWord(),
                wordInfo.getPhonetic(),
                wordInfo.getDefinition(),
                wordInfo.getExample(),
                translatedWord,
                translatedDefinition,
                translatedExample
        );
    }
}
