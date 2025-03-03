package org.engteachbot.service.story;

import org.engteachbot.parser.StoryResponseParser;
import org.engteachbot.response.StoryResponse;
import org.engteachbot.service.GigaChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ITStoryServiceImpl implements ITStoryService {

    private final GigaChatClient gigaChatClient;

    private final StoryResponseParser parser;

    @Autowired
    public ITStoryServiceImpl(GigaChatClient gigaChatClient, StoryResponseParser parser) {
        this.gigaChatClient = gigaChatClient;
        this.parser = parser;
    }

    @Override
    public StoryResponse startStory() {
        String scenePrompt = "Start a short interactive story in English about a junior IT developer, " +
                "2-3 sentences, A2-B1 level, related to coding.";
        String sceneResponse = gigaChatClient.askGigaChat(scenePrompt);
        System.out.println("Raw GigaChat scene response (start): " + sceneResponse);

        String wordsResponse = getWordsForScene(sceneResponse);
        return parser.parse(sceneResponse, wordsResponse);
    }

    @Override
    public StoryResponse continueStory(String previousScene, String userInput) {
        String scenePrompt = String.format(
                "Continue this story: '%s'. The user chose: '%s'. " +
                        "Write 2-3 sentences in English (A2-B1 level).",
                previousScene, userInput
        );
        String sceneResponse = gigaChatClient.askGigaChat(scenePrompt);
        System.out.println("Raw GigaChat scene response (continue): " + sceneResponse);

        String wordsResponse = getWordsForScene(sceneResponse);
        return parser.parse(sceneResponse, wordsResponse);
    }

    private String getWordsForScene(String scene) {
        String wordsPrompt = "Based on this story snippet: '" + scene + "', " +
                "provide 2 IT-related words as JSON array in this exact format: " +
                "[{\"word\": \"word\", \"ipa\": \"[IPA]\", \"translation\": \"translation\", " +
                "\"example\": \"example sentence\", \"translatedExample\": \"translation\"}]. " +
                "Example: [{\"word\": \"debug\", \"ipa\": \"[dɪˈbʌɡ]\", \"translation\": \"отлаживать\", " +
                "\"example\": \"I debug the code.\", \"translatedExample\": \"Я отлаживаю код.\"}]";
        String wordsResponse = gigaChatClient.askGigaChat(wordsPrompt);
        System.out.println("Raw GigaChat words response: " + wordsResponse);
        return wordsResponse;
    }
}
