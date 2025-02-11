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
    private static final int USER_MESSAGE_DELAY = 10;
    private static final int HI_DELAY = 20;
    private static final int DEFAULT_DELAY = 25;
    private static final int START_DELAY = 36000;
    private static final int HELP_DELAY = 100;

    private final MessageService messageService;
    private final DeleteScheduler deleteScheduler;

    public void handleCommand(Message message) {
        var text = (message.getText() == null) ? "" : message.getText().trim();
        var chatId = message.getChatId();
        var userMessageId = message.getMessageId();

        if (!text.startsWith("/")) {
            return;
        }

        deleteScheduler.schedulerDeleteMessage(chatId, userMessageId, USER_MESSAGE_DELAY);

        var userId = message.getFrom().getId();

        var command = text.split("\\s")[0].toLowerCase().split("@")[0];

        switch (command) {
            case "/hi" -> {
                var hi = """
                        👋 Привет! Я бот для проверки лекарств 💊.
                        Чтобы начать, нажмите /start и мы перейдем в личную беседу
                        """;
                var botMessage = messageService.sendText(chatId, hi);
                sendAndDeleteMessage(botMessage, chatId, HI_DELAY);
            }
            case "/start" -> {
                var welcome = "Нажмите кнопку, чтобы посмотреть *сроки годности*.";

                var botMessage = messageService
                        .sendTextWithInLineButton(userId, welcome,
                                "⏰ Показать сроки годности", "CHECK_RED");
                sendAndDeleteMessage(botMessage, userId, START_DELAY);

            }
            case "/help" -> {
                var help = """
                        *Команды:*
                        
                        /start – начать работу
                        /hi – приветствие
                        /help – помощь
                        
                        Используйте /start для начала работы.
                        """;
                var botMessage = messageService.sendText(userId, help);
                sendAndDeleteMessage(botMessage, userId, HELP_DELAY);
            }
            default -> {
                var botMessage = messageService
                        .sendText(userId, "❓ Неизвестная команда. Попробуйте /help.");
                sendAndDeleteMessage(botMessage, userId, DEFAULT_DELAY);
            }
        }
    }
    private void sendAndDeleteMessage(Message botMessage, Long recipientId, int delaySeconds) {
        if (botMessage != null) {
            deleteScheduler.schedulerDeleteMessage(recipientId, botMessage.getMessageId(), delaySeconds);
        }
    }
}