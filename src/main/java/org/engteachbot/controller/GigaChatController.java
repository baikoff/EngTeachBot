package org.engteachbot.controller;

import org.engteachbot.service.GigaChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gigachat")
public class GigaChatController {

    private final GigaChatClient gigaChatClient;

    public GigaChatController(GigaChatClient gigaChatClient) {
        this.gigaChatClient = gigaChatClient;
    }

//    @GetMapping("/models")
//    public ResponseEntity<String> getModels() {
//        return ResponseEntity.ok(gigaChatClient.getAvailableModels());
//    }
}

