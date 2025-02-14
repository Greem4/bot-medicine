package ru.greemlab.botmedicine.telegram;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;

@Getter
@Slf4j
@Component
public class TelegramExecutor extends DefaultAbsSender {

    public TelegramExecutor(@Value("${app.bot.botToken}") String botToken) {
        super(new DefaultBotOptions(), botToken);
    }

    public <T extends Serializable, M extends BotApiMethod<T>> T callApi(M method) {
        try {
            return execute(method);
        } catch (TelegramApiException e) {
            log.error("Ошибка при вызове Telegram API: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка при обращении к Telegram Bot API", e);
        }
    }

    public Message callApiPhoto(SendPhoto photo) {
        try {
            return execute(photo);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке фото: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка при отправке фото через Telegram Bot API", e);
        }
    }
}
