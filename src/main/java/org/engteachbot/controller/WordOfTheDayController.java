package org.engteachbot.controller;

import org.engteachbot.model.WordInfo;
import org.engteachbot.service.WordOfTheDayService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/word-of-the-day")
public class WordOfTheDayController {

    private final WordOfTheDayService wordOfTheDayService;

    public WordOfTheDayController(WordOfTheDayService wordOfTheDayService) {
        this.wordOfTheDayService = wordOfTheDayService;
    }

    @GetMapping
    public ResponseEntity<WordInfo> getWordOfTheDay() {
        try {
            return ResponseEntity.ok(wordOfTheDayService.getWordOfTheDay());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
