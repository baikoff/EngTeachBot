package org.engteachbot.telegram;

import jakarta.transaction.Transactional;
import org.engteachbot.model.LearnedWord;
import org.engteachbot.model.StoryState;
import org.engteachbot.model.WordInfo;
import org.engteachbot.reposiroty.StoryStateRepository;
import org.engteachbot.response.StoryResponse;
import org.engteachbot.service.translate.GigaChatTranslationService;
import org.engteachbot.service.story.ITStoryService;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class Bot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient;
    private final String token;
    private final GigaChatTranslationService translationService;
    private final ITStoryService storyService;
    private final StoryStateRepository storyStateRepository;

    @Autowired
    public Bot(GigaChatTranslationService translationService, ITStoryService storyService,
               StoryStateRepository storyStateRepository, TelegramClient telegramClient) {
        this.translationService = translationService;
        this.storyService = storyService;
        this.storyStateRepository = storyStateRepository;
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
                Long chatIdLong = message.getChatId();
                System.out.println("Received message: " + text + " from chatId: " + chatId);

                if (text.equals("/start")) {
                    sendMessage(chatId, "Привет! Я бот для изучения IT-английского. Напиши '/story', 'Переведи <текст>' или '/words'!");
                } else if (text.equals("/story")) {
                    System.out.println("Processing /story for chatId: " + chatIdLong);
                    StoryState state = storyStateRepository.findById(chatIdLong).orElse(new StoryState());
                    if (state.getChatId() == null) {
                        System.out.println("Starting new story for chatId: " + chatIdLong);
                        state.setChatId(chatIdLong);
                        StoryResponse storyResponse = storyService.startStory();
                        state.setCurrentScene(storyResponse.getSceneText());
                        storyResponse.getWords().forEach(word -> state.addWord(word.getWord(), word.getTranslatedWord(), word.getPhonetic()));
                        storyStateRepository.save(state);
                        sendMessage(chatId, formatStoryMessage(storyResponse));
                    } else {
                        System.out.println("Continuing existing story for chatId: " + chatIdLong);
                        sendMessage(chatId, "Твоя история уже началась! Продолжай: " + state.getCurrentScene() + "\nWhat do you do?");
                    }
                } else if (text.startsWith("Переведи ")) {
                    String wordToTranslate = text.substring(9).trim();
                    String sourceLang = wordToTranslate.matches(".*[а-яА-Я].*") ? "ru" : "en";
                    String targetLang = sourceLang.equals("ru") ? "en" : "ru";
                    String translatedText = translationService.translate(wordToTranslate, sourceLang, targetLang);
                    sendMessage(chatId, translatedText);
                } else if (text.equals("/words")) {
                    StoryState state = storyStateRepository.findById(chatIdLong).orElse(null);
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
                        String messageAboutWords = String.format("Learned words (%d):\n%s", wordCount, wordsList);
                        sendMessage(chatId, messageAboutWords);
                    }
                } else if (storyStateRepository.existsById(chatIdLong)) {
                    System.out.println("Continuing story with input: " + text);
                    StoryState state = storyStateRepository.findById(chatIdLong).get();
                    StoryResponse storyResponse = storyService.continueStory(state.getCurrentScene(), text);
                    state.setCurrentScene(storyResponse.getSceneText());
                    storyResponse.getWords().forEach(word -> state.addWord(word.getWord(), word.getTranslatedWord(), word.getPhonetic()));
                    storyStateRepository.save(state);
                    sendMessage(chatId, formatStoryMessage(storyResponse));
                } else {
                    sendMessage(chatId, "Я не понял команду. Используй '/story', 'Переведи <текст>' или '/words'!");
                }
            }
        }
    }

    private String formatStoryMessage(StoryResponse storyResponse) {
        StringBuilder message = new StringBuilder(storyResponse.getSceneText());
        List<WordInfo> words = storyResponse.getWords();
        if (!words.isEmpty()) {
            message.append("\n\n📚 New words:\n");
            for (WordInfo word : words) {
                message.append(String.format(
                        "- %s [%s] - %s\n  Example: \"%s\" - \"%s\"\n",
                        word.getWord(), word.getPhonetic(), word.getTranslatedWord(),
                        word.getExample(), word.getTranslatedExample()
                ));
            }
        }
        message.append("\nWhat do you do next?");
        return message.toString();
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

