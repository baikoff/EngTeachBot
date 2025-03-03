package org.engteachbot.handler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class StartHandler implements CommandHandler {

    private final TelegramClient telegramClient;

    public StartHandler(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    @Override
    public void handle(String chatId, Long chatIdLong, String text) {
        sendMessage(chatId, "Привет! Я бот для изучения IT-английского. Напиши '/story', 'Переведи <текст>', '/words' или '/quiz'!");
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
