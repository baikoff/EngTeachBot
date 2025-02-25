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
    @Column(length = 4000)
    private String currentScene;
    private Integer sprint;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "learned_words", joinColumns = @JoinColumn(name = "chat_id"))
    @Column(name = "word")
    private List<String> learnedWords = new ArrayList<>();

    public StoryState() {
        this.currentScene = "";
        this.sprint = 1;
    }
}
