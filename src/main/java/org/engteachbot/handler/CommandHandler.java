package org.engteachbot.handler;

public interface CommandHandler {

    void handle(String chatId, Long chatIdLong, String text);
}
