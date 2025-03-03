package org.engteachbot.model;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WordInfo {

    private String word;

    private String translatedWord;

    private String phonetic;

    private String example;

    private String translatedExample;

}
