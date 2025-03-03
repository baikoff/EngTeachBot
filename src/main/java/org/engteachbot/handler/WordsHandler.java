package org.engteachbot.handler;

import org.engteachbot.model.LearnedWord;
import org.engteachbot.reposiroty.StoryStateRepository;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class WordsHandler implements CommandHandler {

    private final StoryStateRepository storyStateRepository;

    private final TelegramClient telegramClient;

    public WordsHandler(StoryStateRepository storyStateRepository, TelegramClient telegramClient) {
        this.storyStateRepository = storyStateRepository;
        this.telegramClient = telegramClient;
    }

    @Override
    public void handle(String chatId, Long chatIdLong, String text) {
        var state = storyStateRepository.findById(chatIdLong).orElse(null);
        if (state == null) {
            sendMessage(chatId, "Ты еще не начал историю, используй /story!");
        } else if (state.getLearnedWords().isEmpty()) {
            sendMessage(chatId, "Ты еще не выучил ни одного слова!");
        } else {
            AtomicInteger counter = new AtomicInteger(1);
            String wordsList = state.getLearnedWords().stream()
                    .sorted(Comparator.comparing(LearnedWord::getWord, String.CASE_INSENSITIVE_ORDER))
                    .map(word -> counter.getAndIncrement() + ". " + word.getWord() + " - " +
                            word.getIpa() + " - " + word.getTranslation())
                    .collect(Collectors.joining("\n"));
            int wordCount = state.getLearnedWords().size();
            String message = String.format("Learned words (%d):\n%s", wordCount, wordsList);
            sendMessage(chatId, message);
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
