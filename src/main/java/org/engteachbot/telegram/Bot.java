package org.engteachbot.telegram;

import org.engteachbot.model.WordInfo;
import org.engteachbot.service.GigaChatTranslationService;
import org.engteachbot.service.GigaChatWordOfTheDayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;


@Component
public class Bot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;
    private final String token;
    private final GigaChatTranslationService translationService;
    private final GigaChatWordOfTheDayService wordOfTheDayService;

    @Autowired
    public Bot(GigaChatTranslationService translationService, GigaChatWordOfTheDayService wordOfTheDayService, TelegramClient telegramClient) {
        this.translationService = translationService;
        this.wordOfTheDayService = wordOfTheDayService;
        this.telegramClient = telegramClient;
        this.token = System.getenv("TOKEN_TG");
        if (this.token == null || this.token.isEmpty()) {
            throw new IllegalStateException("Переменная окружения TOKEN_TG не задана!");
        }
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText()) {
                String chatId = message.getChatId().toString();
                String text = message.getText().trim();

                if (text.equals("/start")) {
                    sendMessage(chatId, "Привет! Я бот для изучения английского. Напиши 'слово дня' или 'Переведи <слово>'");
                } else if (text.equals("слово дня")) {
                    sendWordOfTheDay(chatId);
                } else if (text.startsWith("Переведи ")) {
                    String wordToTranslate = text.substring(9).trim();
                    String sourceLang = wordToTranslate.matches(".*[а-яА-Я].*") ? "ru" : "en";
                    String targetLang = sourceLang.equals("ru") ? "en" : "ru";
                    String translatedText = translationService.translate(wordToTranslate, sourceLang, targetLang);
                    sendMessage(chatId, translatedText);
                }
            }
        }
    }

    private void sendWordOfTheDay(String chatId) {
        WordInfo wordInfo = wordOfTheDayService.getWordOfTheDay();
        System.out.println("WordInfo: " + wordInfo);

        if (wordInfo != null) {
            String message = String.format(
                    "📖 Слово дня: %s\n" +
                            "Перевод: %s\n" +
                            "Транскрипция: %s\n" +
                            "Определение: %s\n" +
                            "Перевод определения: %s\n" +
                            "Пример: %s\n" +
                            "Перевод примера: %s",
                    wordInfo.getWord(),
                    wordInfo.getTranslatedWord(),
                    wordInfo.getPhonetic(),
                    wordInfo.getDefinition(),
                    wordInfo.getTranslatedDefinition(),
                    wordInfo.getExample(),
                    wordInfo.getTranslatedExample()
            );
            sendMessage(chatId, message);
        } else {
            sendMessage(chatId, "Не удалось получить слово дня. Попробуйте позже.");
        }
    }

    private void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage(chatId, text);
        try {
            telegramClient.execute(message);
            System.out.println("Sent to Telegram: " + text);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}

