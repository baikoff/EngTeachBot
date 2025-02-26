package org.engteachbot.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "story_state")
@Getter
@Setter
@AllArgsConstructor
public class StoryState {

    @Id
    private Long chatId;

    @Column(columnDefinition = "TEXT")
    private String currentScene;

    private Integer sprint;

    @OneToMany(mappedBy = "storyState", cascade = CascadeType.ALL,orphanRemoval = true, fetch = FetchType.EAGER)
    private List<LearnedWord> learnedWords = new ArrayList<>();

    public StoryState() {
        this.currentScene = "";
        this.sprint = 1;
    }

    public void addWord(String word, String translation, String ipa) {
        if (!getLearnedWords().stream().anyMatch(w -> w.getWord().equals(word))) {
            LearnedWord learnedWord = new LearnedWord(word, translation, ipa, this);
            learnedWords.add(learnedWord);
        }
    }

    public List<String> getWordStrings() {
        return learnedWords.stream().map(LearnedWord::getWord).toList();
    }
}
