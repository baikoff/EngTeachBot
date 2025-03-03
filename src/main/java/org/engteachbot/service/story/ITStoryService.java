package org.engteachbot.service.story;

import org.engteachbot.response.StoryResponse;

public interface ITStoryService {

    StoryResponse startStory();

    StoryResponse continueStory(String previousScene, String userInput);
}
