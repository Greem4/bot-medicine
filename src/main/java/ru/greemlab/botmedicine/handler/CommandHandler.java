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

        var command = text.split("\\s")[0].toLowerCase().split("@")[0];

        deleteScheduler.schedulerDeleteMessage(chatId, userMessageId, 10);

        switch (command) {
            case "/hi" -> {
                var hi = """
                        👋 Привет! Я бот для проверки лекарств 💊.
                        Чтобы начать, нажмите /start
                        """;
                var botMessage = messageService.sendText(chatId, hi);
                sendAndDeleteMessage(botMessage, chatId, 20);
            }
            case "/start" -> {
                var welcome = "Нажмите кнопку, чтобы посмотреть *сроки годности*.";
                var botMessage = messageService.sendTextWithInLineButton(chatId, welcome,
                        "⏰ Показать сроки годности", "CHECK_RED");
                sendAndDeleteMessage(botMessage, chatId, 36000);
            }
            case "/help" -> {
                var help = """
                        *Команды:*
                        
                        /start – начать работу
                        /hi – приветствие
                        /help – помощь
                        
                        Используйте /start для начала работы.
                        """;
                var botMessage = messageService.sendText(chatId, help);
                sendAndDeleteMessage(botMessage, chatId, 100);
            }
            default -> {
                var botMessage = messageService
                        .sendText(chatId, "❓ Неизвестная команда. Попробуйте /help.");
                sendAndDeleteMessage(botMessage, chatId, 20);
            }
        }
    }
    private void sendAndDeleteMessage(Message botMessage, Long chatId, int delaySeconds) {
        if (botMessage != null) {
            deleteScheduler.schedulerDeleteMessage(chatId, botMessage.getMessageId(), delaySeconds);
        }
    }
}