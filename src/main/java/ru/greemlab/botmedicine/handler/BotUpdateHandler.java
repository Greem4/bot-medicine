package ru.greemlab.botmedicine.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class BotUpdateHandler {

    private final CommandHandler commandHandler;
    private final AdminCommandHandler adminCommandHandler;
    private final CallbackQueryHandler callbackQueryHandler;

    public void handleUpdate(Update update) {
        if (update.hasMessage()) {
            if (update.getMessage().hasText() && update.getMessage().getText().startsWith("/")) {
                commandHandler.handleCommand(update.getMessage());
            } else {
                adminCommandHandler.handleRegularText(update.getMessage());
            }
        } else if (update.hasCallbackQuery()) {
            callbackQueryHandler.handleCallback(update.getCallbackQuery());
        }
    }
}
