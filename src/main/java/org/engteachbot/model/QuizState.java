package org.engteachbot.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuizState {

    private String word;

    private String correctAnswer;

    private List<String> options;

    private int score;

    private int questionCount;

    private static final int MAX_QUESTIONS = 3;

    public QuizState(String word, String correctAnswer, List<String> options) {
        this.word = word;
        this.correctAnswer = correctAnswer;
        this.options = options;
        this.score = 0;
        this.questionCount = 0;
    }

    public boolean hasNextQuestion() {
        return questionCount < MAX_QUESTIONS;
    }

    public void incrementQuestionCount() {
        questionCount++;
    }

    public void incrementScore() {
        score++;
    }
}
