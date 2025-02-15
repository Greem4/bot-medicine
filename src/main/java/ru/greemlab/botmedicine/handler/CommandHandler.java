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
    private static final int DELAY_15 = 15;
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
        var userName = message.getFrom().getUserName();

        if (!text.startsWith("/")) {
            return;
        }

        deleteScheduler.schedulerDeleteMessage(chatId, userMessageId, DELAY_10);

        var userId = message.getFrom().getId();

        var parts = text.split("\\s+");
        var command = text.split("\\s")[0].toLowerCase().split("@")[0];

        if (chatId < 0) {
            var registered = groupScheduleService.isGroupRegistered(chatId);
            if (registered) {
                authorizedGroupUserService.addUser(userName,userId, chatId);
                log.info("Пользователь {} добавлен в группу {}", userId, chatId);
            }
        }

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
                var botMessage = messageService
                        .sendTextWithTwoInlineButtons(
                                userId,
                                welcome,
                                "⏰ Сроки годности", "CHECK_RED",
                                "📅 График", "VIEW_SCHEDULE"
                        );
                sendAndDeleteMessage(botMessage, userId, DELAY_DAY);

            }
            case "/schedule" -> {
                    if (!groupScheduleService.isGroupRegistered(chatId)) {
                        var notRegMsg = messageService.sendText(chatId,
                                "Эта группа не зарегистрирована, график недоступен.");
                        sendAndDeleteMessage(notRegMsg, userId, DELAY_10);
                    } else {
                        if (authorizedGroupUserService.isUserAuthorized(userName, userId, chatId)) {
                            var scheduleOpt = groupScheduleService.findSchedulerUrl(chatId);
                            if (scheduleOpt.isPresent()) {
                                var scheduleUrl = scheduleOpt.get();
                                var botMessage = messageService.sendPhoto(userId, scheduleUrl, "График");
                                sendAndDeleteMessage(botMessage, userId, DELAY_25);
                            } else {
                                var msg = messageService.sendText(chatId, "Ссылка не найдена");
                                sendAndDeleteMessage(msg, userId, DELAY_25);
                            }
                        } else {
                            var msg = messageService.sendText(chatId,
                                    "У вас нет доступа. Вы не в списке для этой группы.");
                            sendAndDeleteMessage(msg, userId, DELAY_10);
                        }
                    }
            }
            case "/setgroup" -> {
                if (!userId.equals(admin)) {
                    var msg = messageService.sendText(chatId, "Только админ может выполнять эту команду.");
                    sendAndDeleteMessage(msg, chatId, DELAY_15);
                }
                if (parts.length < 3) {
                    var msg = messageService.sendText(chatId, "Использование: /setgroup <groupChatId> <scheduleUrl>");
                    sendAndDeleteMessage(msg, chatId, DELAY_15);
                }
                try {
                    var grpId = Long.parseLong(parts[1]);
                    var url = parts[2];
                    groupScheduleService.upsertGroup(grpId, url);
                    var okMsg = messageService.sendText(chatId,
                            "Группа " + grpId + " зарегистрирована/обновлена со ссылкой: " + url);
                    sendAndDeleteMessage(okMsg, chatId, DELAY_15);
                } catch (NumberFormatException e) {
                    var msg = messageService.sendText(chatId, "Неверный формат groupChatId.");
                    sendAndDeleteMessage(msg, chatId, 10);
                }
            }

            case "/removeuser" -> {
                if (!userId.equals(admin)) {
                    var msg = messageService
                            .sendText(chatId, "Только админ может выполнять эту команду.");
                    sendAndDeleteMessage(msg, chatId, DELAY_15);
                }
                if (parts.length < 3) {
                    var msg = messageService.sendText(chatId,
                            "Использование: /removeuser <groupChatId> <userId>");
                    sendAndDeleteMessage(msg, chatId, DELAY_15);
                }
                try {
                    var grpId = Long.parseLong(parts[1]);
                    var rmUserId = Long.parseLong(parts[2]);
                    authorizedGroupUserService.removeUser(userName, grpId, rmUserId);
                    var msg = messageService.sendText(chatId,
                            "Пользователь " + rmUserId + " удалён из группы " + grpId + ".");
                    sendAndDeleteMessage(msg, chatId, DELAY_15);
                } catch (NumberFormatException e) {
                    var msg = messageService.sendText(chatId, "Неверный формат аргументов.");
                    sendAndDeleteMessage(msg, chatId, DELAY_15);
                }
            }
            case "/help" -> {
                var help = """
                        *Команды:*
                        
                        /start – начать работу
                        /hi – приветствие
                        /help – помощь
                        /schedule — показать график для *текущей* группы (если вы в списке)
                        
                        *Админские*:
                        /setgroup <groupChatId> <scheduleUrl> — зарегистрировать/обновить группу с её ссылкой
                        /removeuser <groupChatId> <userId>
                        
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