package org.engteachbot.handler;

import lombok.Getter;
import org.engteachbot.model.LearnedWord;
import org.engteachbot.model.QuizState;
import org.engteachbot.reposiroty.StoryStateRepository;
import org.engteachbot.service.GigaChatClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.*;

@Component
public class QuizHandler implements CommandHandler {

    private final StoryStateRepository storyStateRepository;

    private final GigaChatClient gigaChatClient;

    private final TelegramClient telegramClient;
    @Getter
    private final Map<Long, QuizState> quizStates = new HashMap<>();

    private final Map<Long, List<LearnedWord>> remainingWords = new HashMap<>();

    public QuizHandler(StoryStateRepository storyStateRepository, GigaChatClient gigaChatClient, TelegramClient telegramClient) {
        this.storyStateRepository = storyStateRepository;
        this.gigaChatClient = gigaChatClient;
        this.telegramClient = telegramClient;
    }

    @Override
    public void handle(String chatId, Long chatIdLong, String text) {
        if (text.equals("/quiz")) {
            startQuiz(chatId, chatIdLong);
        } else if (quizStates.containsKey(chatIdLong)) {
            handleQuizAnswer(chatId, chatIdLong, text);
        }
    }

    private void startQuiz(String chatId, Long chatIdLong) {
        var state = storyStateRepository.findById(chatIdLong).orElse(null);
        if (state == null || state.getLearnedWords().isEmpty()) {
            sendMessage(chatId, "Ты еще не выучил слов для квиза! Начни с /story");
        } else {
            remainingWords.put(chatIdLong, new ArrayList<>(state.getLearnedWords()));
            askNextQuestion(chatId, chatIdLong);
        }
    }

    private void askNextQuestion(String chatId, Long chatIdLong) {
        List<LearnedWord> words = remainingWords.get(chatIdLong);
        if (words.isEmpty()) {
            QuizState quizState = quizStates.get(chatIdLong);
            sendMessage(chatId, "Квиз завершен! Твой результат: " + quizState.getScore() + "/3");
            quizStates.remove(chatIdLong);
            remainingWords.remove(chatIdLong);
            return;
        }

        Random random = new Random();
        LearnedWord word = words.remove(random.nextInt(words.size()));
        String prompt = "Дай 3 случайных перевода IT-слов на русский, кроме '" + word.getTranslation() + "' " +
                "в формате JSON: [\"перевод1\", \"перевод2\", \"перевод3\"]";
        String response = gigaChatClient.askGigaChat(prompt);
        System.out.println("GigaChat quiz response: " + response);
        List<String> options = parseQuizOptions(response, word.getTranslation());
        QuizState quizState = quizStates.computeIfAbsent(chatIdLong, k -> new QuizState(word.getWord(), word.getTranslation(), options));
        quizState.setWord(word.getWord());
        quizState.setCorrectAnswer(word.getTranslation());
        quizState.setOptions(options);
        quizState.incrementQuestionCount();

        StringBuilder quizMessage = new StringBuilder("Вопрос " + quizState.getQuestionCount() + "/3: Что значит '" + word.getWord() + "'?\n");
        for (int i = 0; i < options.size(); i++) {
            quizMessage.append((i + 1)).append(". ").append(options.get(i)).append("\n");
        }
        quizMessage.append("Напиши номер правильного ответа!");
        sendMessage(chatId, quizMessage.toString());
    }

    private void handleQuizAnswer(String chatId, Long chatIdLong, String text) {
        QuizState quizState = quizStates.get(chatIdLong);
        try {
            int userAnswer = Integer.parseInt(text) - 1;
            List<String> options = quizState.getOptions();
            if (userAnswer >= 0 && userAnswer < options.size()) {
                String selectedAnswer = options.get(userAnswer);
                if (selectedAnswer.equals(quizState.getCorrectAnswer())) {
                    quizState.incrementScore();
                    sendMessage(chatId, "Правильно! '" + quizState.getWord() + "' значит '" + quizState.getCorrectAnswer() + "'");
                } else {
                    sendMessage(chatId, "Неправильно! '" + quizState.getWord() + "' значит '" + quizState.getCorrectAnswer() + "'");
                }
                if (quizState.hasNextQuestion()) {
                    askNextQuestion(chatId, chatIdLong);
                } else {
                    sendMessage(chatId, "Квиз завершен! Твой результат: " + quizState.getScore() + "/3");
                    quizStates.remove(chatIdLong);
                    remainingWords.remove(chatIdLong);
                }
            } else {
                sendMessage(chatId, "Пожалуйста, выбери номер от 1 до " + options.size());
            }
        } catch (NumberFormatException e) {
            sendMessage(chatId, "Напиши номер ответа цифрой!");
        }
    }

    private List<String> parseQuizOptions(String response, String correctAnswer) {
        List<String> options = new ArrayList<>();
        try {
            int startIndex = response.indexOf("[");
            int endIndex = response.lastIndexOf("]") + 1;
            if (startIndex != -1 && startIndex < endIndex) {
                String jsonPart = response.substring(startIndex, endIndex).trim();
                JSONArray jsonArray = new JSONArray(jsonPart);
                for (int i = 0; i < jsonArray.length(); i++) {
                    options.add(jsonArray.getString(i));
                }
            } else {
                throw new JSONException("JSON array not found in response");
            }
        } catch (Exception e) {
            System.out.println("Failed to parse quiz options: " + response);
            e.printStackTrace();
            options.add("программа");
            options.add("цикл");
            options.add("алгоритм");
        }
        Random random = new Random();
        options.add(random.nextInt(4), correctAnswer);
        return options;
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
