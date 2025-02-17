package org.engteachbot.model;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WordInfo {
    private String word;
    private String phonetic;
    private String definition;
    private String example;
    private String translatedWord;
    private String translatedDefinition;
    private String translatedExample;
}
