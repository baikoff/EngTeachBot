package org.engteachbot.handler;

import org.engteachbot.service.translate.TranslationService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class TranslateHandler implements CommandHandler {

    private final TranslationService translationService;

    private final TelegramClient telegramClient;

    public TranslateHandler(TranslationService translationService, TelegramClient telegramClient) {
        this.translationService = translationService;
        this.telegramClient = telegramClient;
    }

    @Override
    public void handle(String chatId, Long chatIdLong, String text) {
        String wordToTranslate = text.substring(9).trim();
        String sourceLang = wordToTranslate.matches(".*[а-яА-Я].*") ? "ru" : "en";
        String targetLang = sourceLang.equals("ru") ? "en" : "ru";
        String translatedText = translationService.translate(wordToTranslate, sourceLang, targetLang);
        sendMessage(chatId, translatedText);
    }

    private void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage(chatId, text);
        try {
            telegramClient.execute(message);
            System.out.println("Sent to Telegram: " + text);
        } catch (TelegramApiException e) {
            System.err.println("Failed to send message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
