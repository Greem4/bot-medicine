package ru.greemlab.botmedicine.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.greemlab.botmedicine.service.AdminConversationService;
import ru.greemlab.botmedicine.service.AuthorizedGroupUserService;
import ru.greemlab.botmedicine.service.GroupScheduleService;
import ru.greemlab.botmedicine.service.MessageService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminCommandHandler {

    private final MessageService messageService;
    private final AdminConversationService adminConversationService;
    private final GroupScheduleService groupScheduleService;
    private final AuthorizedGroupUserService authorizedGroupUserService;

    public void handleRegularText(Message message) {
        var chatId = message.getChatId();
        var userName = message.getFrom().getUserName();
        var userId = message.getFrom().getId();
        var text = message.getText();

        if (messageService.isAdmin(userId)) {
            var state = adminConversationService.getCurrentState(userId);
            switch (state) {
                case WAITING_FOR_SETGROUP_DATE -> {
                    try {
                        var parts = text.split("\\s+", 2);
                        var grpId = Long.parseLong(parts[0]);
                        var scheduleUrl = parts[1];
                        groupScheduleService.upsertGroup(grpId, scheduleUrl);

                        messageService.sendText(chatId,
                                "Группа" + grpId + " зарегистрирована/обновлена со ссылкой: " + scheduleUrl);
                    } catch (Exception e) {
                        messageService.sendText(chatId,
                                "Ошибка формата. Нужно: groupChatId scheduleUrl");
                    }
                    adminConversationService.clearState(userId);
                }
                case WAITING_FOR_REMOVEGPOUP_DATE -> {
                    try {
                        var grpId = Long.parseLong(text);
                        groupScheduleService.deleteGroup(grpId);
                        messageService.sendText(chatId,
                                "Группа " + grpId + " удалена.");
                    } catch (Exception e) {
                        messageService.sendText(chatId,
                                "Ошибка. Нужно число (groupChatId).");
                    }
                    adminConversationService.clearState(userId);
                }
                case WAITING_FOR_REMOVEUSER_DATE -> {
                    try {
                        var parts = text.split("\\s+");
                        var grpId = Long.parseLong(parts[0]);
                        var removeUserId = Long.parseLong(parts[1]);
                        authorizedGroupUserService.removeUser(userName, grpId, removeUserId);
                        messageService.sendText(chatId,
                                "Пользователь "  + userName + " удалён из группы " + grpId + ".");

                    } catch (Exception e) {
                        messageService.sendText(chatId,
                                "Ошибка формата. Нужно: groupChatId userId");
                    }
                    adminConversationService.clearState(userId);
                }

                default -> {
                    messageService.sendText(userId, "не спим");
                }
            }
        }
    }

}
