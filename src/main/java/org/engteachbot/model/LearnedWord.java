package org.engteachbot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "learned_words")
@Getter
@Setter
@NoArgsConstructor
public class LearnedWord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "translation")
    private String translation;

    @Column(name = "ipa")
    private String ipa;

    @Column(name = "word", nullable = false)
    private String word;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private StoryState storyState;

    public LearnedWord(String word, String translation, String ipa, StoryState storyState) {
        this.word = word;
        this.translation = translation;
        this.ipa = ipa;
        this.storyState = storyState;
    }
}
