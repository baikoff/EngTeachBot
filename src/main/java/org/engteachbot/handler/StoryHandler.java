package org.engteachbot.handler;

import org.engteachbot.model.StoryState;
import org.engteachbot.reposiroty.StoryStateRepository;
import org.engteachbot.response.StoryResponse;
import org.engteachbot.service.story.ITStoryService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class StoryHandler implements CommandHandler {

    private final StoryStateRepository storyStateRepository;

    private final ITStoryService storyService;

    private final TelegramClient telegramClient;

    public StoryHandler(StoryStateRepository storyStateRepository, ITStoryService storyService, TelegramClient telegramClient) {
        this.storyStateRepository = storyStateRepository;
        this.storyService = storyService;
        this.telegramClient = telegramClient;
    }

    @Override
    public void handle(String chatId, Long chatIdLong, String text) {
        StoryState state = storyStateRepository.findById(chatIdLong).orElse(new StoryState());
        if (text.equals("/story")) {
            if (state.getChatId() == null) {
                System.out.println("Starting new story for chatId: " + chatIdLong);
                state.setChatId(chatIdLong);
                var storyResponse = storyService.startStory();
                state.setCurrentScene(storyResponse.getSceneText());
                storyResponse.getWords().forEach(word -> state.addWord(word.getWord(), word.getTranslatedWord(), word.getPhonetic()));
                storyStateRepository.save(state);
                sendMessage(chatId, formatStoryMessage(storyResponse));
            } else {
                System.out.println("Continuing existing story for chatId: " + chatIdLong);
                sendMessage(chatId, "Твоя история уже началась! Продолжай: " + state.getCurrentScene() + "\nWhat do you do?");
            }
        } else {
            System.out.println("Continuing story with input: " + text);
            var storyResponse = storyService.continueStory(state.getCurrentScene(), text);
            state.setCurrentScene(storyResponse.getSceneText());
            storyResponse.getWords().forEach(word -> state.addWord(word.getWord(), word.getTranslatedWord(), word.getPhonetic()));
            storyStateRepository.save(state);
            sendMessage(chatId, formatStoryMessage(storyResponse));
        }
    }

    private String formatStoryMessage(StoryResponse storyResponse) {
        StringBuilder message = new StringBuilder(storyResponse.getSceneText());
        var words = storyResponse.getWords();
        if (!words.isEmpty()) {
            message.append("\n\n📚 New words:\n");
            for (var word : words) {
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
