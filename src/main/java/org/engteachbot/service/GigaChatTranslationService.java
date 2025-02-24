package org.engteachbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GigaChatTranslationService implements TranslationService {

    private final GigaChatClient gigaChatClient;

    @Autowired
    public GigaChatTranslationService(GigaChatClient gigaChatClient) {
        this.gigaChatClient = gigaChatClient;
    }

    @Override
    public String translate(String text, String sourceLang, String targetLang) {
        String prompt = String.format("Переведи текст с %s на %s: %s",
                sourceLang.equals("ru") ? "русского" : "английского",
                targetLang.equals("ru") ? "русский" : "английский",
                text);
        return gigaChatClient.askGigaChat(prompt);
    }
}

