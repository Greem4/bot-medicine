package ru.greemlab.botmedicine.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.greemlab.botmedicine.scheduler.DeleteScheduler;
import ru.greemlab.botmedicine.service.AuthorizedGroupUserService;
import ru.greemlab.botmedicine.service.GroupScheduleService;
import ru.greemlab.botmedicine.service.MessageService;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandHandler {
    private static final int DELAY_10 = 10;
    private static final int DELAY_20 = 20;
    private static final int DELAY_25 = 25;
    private static final int DELAY_100 = 100;
    private static final int DELAY_DAY = 36000;

    @Value("${app.bot.admin-id}")
    private Long admin;

    private final MessageService messageService;
    private final DeleteScheduler deleteScheduler;

    private final GroupScheduleService groupScheduleService;
    private final AuthorizedGroupUserService authorizedGroupUserService;


    public void handleCommand(Message message) {
        var text = (message.getText() == null) ? "" : message.getText().trim();
        var chatId = message.getChatId();
        var userMessageId = message.getMessageId();

        if (!text.startsWith("/")) {
            return;
        }

        deleteScheduler.schedulerDeleteMessage(chatId, userMessageId, DELAY_10);

        var userName = message.getFrom().getUserName();
        var userId = message.getFrom().getId();

        if (chatId < 0) {
            var registered = groupScheduleService.isGroupRegistered(chatId);
            if (registered) {
                authorizedGroupUserService.addUser(userName,userId, chatId);
                log.info("Пользователь {} добавлен в группу {}", userId, chatId);
            }
        }

        var command = text.split("\\s")[0].toLowerCase().split("@")[0];

        switch (command) {
            case "/hi" -> {
                var hi = """
                        👋 Привет! Я бот для проверки лекарств 💊.
                        Чтобы начать, нажмите /start и мы перейдем в личную беседу
                        """;
                var botMessage = messageService.sendText(chatId, hi);
                sendAndDeleteMessage(botMessage, chatId, DELAY_20);
            }
            case "/start" -> {
                var welcome = """
                        *Добро пожаловать!*
                        • Проверить *сроки годности* лекарств.
                        • Посмотреть *график*.
                        """;
                var botMessage = messageService.sendMainMenu(userId, welcome, userId.equals(admin));
                sendAndDeleteMessage(botMessage, userId, DELAY_DAY);

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
                sendAndDeleteMessage(botMessage, userId, DELAY_100);
            }
            default -> {
                var botMessage = messageService
                        .sendText(userId, "❓ Неизвестная команда. Попробуйте /help.");
                sendAndDeleteMessage(botMessage, userId, DELAY_25);
            }
        }
    }

    private void sendAndDeleteMessage(Message botMessage, Long recipientId, int delaySeconds) {
        if (botMessage != null) {
            deleteScheduler.schedulerDeleteMessage(recipientId, botMessage.getMessageId(), delaySeconds);
        }
    }
}