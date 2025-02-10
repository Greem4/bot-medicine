package ru.greemlab.botmedicine.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.greemlab.botmedicine.scheduler.DeleteScheduler;
import ru.greemlab.botmedicine.service.MessageService;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandHandler {

    private final MessageService messageService;
    private final DeleteScheduler deleteScheduler;

    public void handleCommand(Message message) {
        var text = message.getText().trim();
        var chatId = message.getChatId();
        var userMessageId = message.getMessageId();

        var command = text.split("\\s")[0].toLowerCase();
        final var BOT_NAME = "greem_lab_bot";

        switch (command) {
            case "/hi", "/hi@" + BOT_NAME -> {
                var hi = """
                        👋 Привет! Я бот для проверки лекарств 💊.
                           Чтобы начать, нажмите /start
                        """;
                messageService.sendText(chatId, hi);
            }
            case "/start", "/start@" + BOT_NAME -> {
                var welcome = "Нажмите кнопку, чтобы посмотреть *сроки годности*.";
                messageService.sendTextWithInLineButton(chatId, welcome,
                        "⏰ Показать сроки годности", "CHECK_RED");
            }
            case "/help", "/help@" + BOT_NAME -> {
                var help = """
                        *Команды:*
                        
                        /start – начать работу
                        /hi – приветствие
                        /help – помощь
                        
                        Используйте /start для начала работы.
                        """;
                messageService.sendText(chatId, help);
            }
            default ->
                    messageService.sendText(chatId, "❓ Неизвестная команда. Попробуйте /help.");
        }
        deleteScheduler.schedulerDeleteMessage(chatId, userMessageId, 10);
    }
}