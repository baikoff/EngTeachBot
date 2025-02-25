package org.engteachbot.controller;

import org.engteachbot.service.translate.GigaChatTranslationService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dictionary")
public class DictionaryController {


    private final GigaChatTranslationService translationService;

    public DictionaryController( GigaChatTranslationService translationService) {
        this.translationService = translationService;
    }

//    @GetMapping("/{word}")
//    public ResponseEntity<WordInfo> getWordInfo(@PathVariable String word) {
//        try {
//            WordInfo wordInfo = dictionaryClient.fetchWordInfo(word);
//            if (wordInfo != null) {
//                wordInfo.setTranslatedWord(translationService.translate(wordInfo.getWord(), "en", "ru"));
//                wordInfo.setTranslatedDefinition(translationService.translate(wordInfo.getDefinition(), "en", "ru"));
//                wordInfo.setTranslatedExample(
//                        wordInfo.getExample().equals("Нет примера") ? "Нет примера"
//                                : translationService.translate(wordInfo.getExample(), "en", "ru"));
//                return ResponseEntity.ok(wordInfo);
//            } else {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
//            }
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//    }
}
