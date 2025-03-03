package org.engteachbot.telegram;

import org.engteachbot.handler.*;
import org.engteachbot.reposiroty.StoryStateRepository;
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

import java.util.HashMap;
import java.util.Map;

@Component
public class Bot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;

    private final String token;

    private final Map<String, CommandHandler> commandHandlers;

    private final QuizHandler quizHandler;

    private final StoryStateRepository storyStateRepository;

    @Autowired
    public Bot(TelegramClient telegramClient, StartHandler startHandler, StoryHandler storyHandler,
               TranslateHandler translateHandler, WordsHandler wordsHandler, QuizHandler quizHandler,
               StoryStateRepository storyStateRepository) {
        this.telegramClient = telegramClient;
        this.quizHandler = quizHandler;
        this.storyStateRepository = storyStateRepository;
        this.token = System.getenv("TOKEN_TG");
        if (this.token == null || this.token.isEmpty()) {
            throw new IllegalStateException("Переменная окружения TOKEN_TG не задана!");
        }
        this.commandHandlers = new HashMap<>();
        commandHandlers.put("/start", startHandler);
        commandHandlers.put("/story", storyHandler);
        commandHandlers.put("Переведи ", translateHandler);
        commandHandlers.put("/words", wordsHandler);
        commandHandlers.put("/quiz", quizHandler);
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
                Long chatIdLong = message.getChatId();
                System.out.println("Received message: " + text + " from chatId: " + chatId);

                CommandHandler handler = commandHandlers.entrySet().stream()
                        .filter(entry -> text.startsWith(entry.getKey()))
                        .map(Map.Entry::getValue)
                        .findFirst()
                        .orElse(null);

                if (handler != null) {
                    handler.handle(chatId, chatIdLong, text);
                } else if (quizHandler.getQuizStates().containsKey(chatIdLong)) {
                    quizHandler.handle(chatId, chatIdLong, text);
                } else if (storyStateRepository.existsById(chatIdLong)) {
                    commandHandlers.get("/story").handle(chatId, chatIdLong, text);
                } else {
                    sendMessage(chatId, "Я не понял команду. Используй '/story', 'Переведи <текст>', '/words' или '/quiz'!");
                }
            }
        }
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

