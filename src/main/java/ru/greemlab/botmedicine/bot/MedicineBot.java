package ru.greemlab.botmedicine.bot;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.greemlab.botmedicine.handler.CallbackQueryHandler;
import ru.greemlab.botmedicine.handler.CommandHandler;

@Slf4j
@Getter
@Component
@RequiredArgsConstructor
public class MedicineBot extends TelegramLongPollingBot {

    @Value("${app.bot.botUsername}")
    private String botUsername;
    @Value("${app.bot.botToken}")
    private String botToken;

    private final CommandHandler commandHandler;
    private final CallbackQueryHandler callbackHandler;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            callbackHandler.handleCallback(update.getCallbackQuery());
        }
        if (update.hasMessage() && update.getMessage().hasText()) {
            commandHandler.handleCommand(update.getMessage());
        }
    }
}
