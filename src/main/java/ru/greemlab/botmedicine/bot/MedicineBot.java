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
import ru.greemlab.botmedicine.service.AdminCommandHandlerService;

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
    private final AdminCommandHandlerService adminCommandHandlerService;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            var msg = update.getMessage();
            if (msg.isCommand()) {
                commandHandler.handleCommand(msg);
            } else {
                adminCommandHandlerService.handleRegularText(msg);
            }
        } else {
            callbackHandler.handleCallback(update.getCallbackQuery());
        }
    }
}
