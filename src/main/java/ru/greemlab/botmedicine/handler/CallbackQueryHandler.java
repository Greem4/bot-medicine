package ru.greemlab.botmedicine.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.greemlab.botmedicine.dto.MedicineResponse;
import ru.greemlab.botmedicine.service.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CallbackQueryHandler {
    private static final String CALLBACK_CHECK_RED = "CHECK_RED";
    public static final String CALLBACK_VIEW_SCHEDULE = "VIEW_SCHEDULE";

    private static final String CALLBACK_ADMIN_MENU = "ADMIN_MENU";
    private static final String CALLBACK_ADMIN_SET_GROUP = "ADMIN_SET_GROUP";
    private static final String CALLBACK_ADMIN_UPDATE_SCHEDULE = "ADMIN_UPDATE_SCHEDULE";
    private static final String CALLBACK_ADMIN_REMOVE_GROUP = "ADMIN_REMOVE_GROUP";
    private static final String CALLBACK_ADMIN_REMOVE_USER = "ADMIN_REMOVE_USER";

    private static final String PROCESSING_MESSAGE = "Запрос обрабатывается…";
    private static final String NO_MEDICINES_MESSAGE = "✅ Нет просроченных лекарств.";
    private static final String ERROR_MESSAGE = "❗ Произошла ошибка при запросе к серверу.";
    private static final String UNKNOWN_CALLBACK_MESSAGE = "❓ Неизвестное действие.";
    private static final String HEADER = "*Сроки годности менее 30 дней:*\n\n";

    private final MedicineService medicineService;
    private final MessageService messageService;
    private final GroupScheduleService groupScheduleService;
    private final AuthorizedGroupUserService authorizedGroupUserService;
    private final AdminConversationService adminConversationService;

    public void handleCallback(CallbackQuery callbackQuery) {
        var userId = callbackQuery.getFrom().getId();
        var messageId = callbackQuery.getMessage().getMessageId();
        var callbackData = callbackQuery.getData();
        var callbackQueryId = callbackQuery.getId();

        messageService.answerCallbackQuery(callbackQueryId, PROCESSING_MESSAGE, false);

        switch (callbackData) {
            case CALLBACK_CHECK_RED -> handleCheckRedExpiration(userId, messageId, true);
            case CALLBACK_VIEW_SCHEDULE -> handleViewSchedule(userId, messageId, true);

            case CALLBACK_ADMIN_MENU -> handleAdminMenuCallback(userId, messageId);
            case CALLBACK_ADMIN_SET_GROUP -> handleAdminSetGroupCallback(userId, messageId);
            case CALLBACK_ADMIN_UPDATE_SCHEDULE -> handleAdminUpdateSchedule(userId, messageId);
            case CALLBACK_ADMIN_REMOVE_GROUP -> handleAdminRemoveGroupCallback(userId, messageId);
            case CALLBACK_ADMIN_REMOVE_USER -> handleAdminRemoveUserCallback(userId, messageId);

            default -> {
                log.warn("Неизвестное значение callback: {}", callbackData);
                messageService.sendText(userId, UNKNOWN_CALLBACK_MESSAGE);
            }
        }

    }

    public void handleCheckRedExpiration(Long chatId, Integer originalMsgId, boolean isCallback) {
        try {
            List<MedicineResponse.MedicineViewList> redList = medicineService.getRedMedicines();
            if (redList == null || redList.isEmpty()) {
                sendOrEdit(chatId, originalMsgId, NO_MEDICINES_MESSAGE, isCallback);
            } else {
                String formatted = redList.stream()
                        .map(this::formatMedicine)
                        .collect(Collectors.joining());
                sendOrEdit(chatId, originalMsgId, HEADER + formatted, isCallback);
            }
        } catch (Exception e) {
            log.error("Ошибка при получении списка лекарств", e);
            sendOrEdit(chatId, originalMsgId, ERROR_MESSAGE, isCallback);
        }
    }

    public void handleViewSchedule(Long userId, Integer originalMsgId, boolean isCallback) {
        try {
            var groups = authorizedGroupUserService.findGroupsForUser(userId);
            if (groups.isEmpty()) {
                sendOrEdit(userId, originalMsgId,
                        "У вас нет доступа ни к одной группе.", isCallback);
            }
            var groupId = groups.getFirst();
            var schedulerOpt = groupScheduleService.findSchedulerUrl(groupId);
            if (schedulerOpt.isEmpty()) {
                sendOrEdit(userId, originalMsgId,
                        "Ссылка на график для вашей группы не найдена.", isCallback);
            }

            var scheduleUrl = schedulerOpt.get();

            messageService.sendPhoto(userId, scheduleUrl, "График");

        } catch (Exception e) {
            log.error("Ошибка при получении графика", e);
            sendOrEdit(userId, originalMsgId,
                    "Произошла ошибка при получении графика.", isCallback);
        }
    }

    private void sendOrEdit(Long chatId, Integer messageId, String text, boolean isCallback) {
        if (isCallback && messageId != null) {
            messageService.editText(chatId, messageId, text);
        } else {
            messageService.sendText(chatId, text);
        }
    }

    private String formatMedicine(MedicineResponse.MedicineViewList m) {
        return String.format("""
                        • *%s* (%s)
                          Срок годности до:
                          ➡*%s*
                        """,
                m.name(),
                m.serialNumber(),
                m.expirationDate()
        );
    }

    private void handleAdminMenuCallback(Long userId, Integer originalMessageId) {
        rights(userId, originalMessageId);
        messageService.editTextToAdminMenu(userId, originalMessageId,
                "Админ-меню. Выберите действие:");
    }

    private void handleAdminSetGroupCallback(Long userId, Integer originalMessageId) {
        rights(userId, originalMessageId);
        messageService.editText(userId, originalMessageId,
                "Отправьте мне в личку сообщение в формате:\n\n`groupChatId scheduleUrl`");
        adminConversationService.waitForSetGroupData(userId);
    }

    private void handleAdminUpdateSchedule(Long userId, Integer originalMessageId) {
        rights(userId, originalMessageId);
        messageService.editText(userId, originalMessageId,
                "Отправьте url адрес графика");
        adminConversationService.waitForSetScheduleUrl(userId);
    }

    private void handleAdminRemoveGroupCallback(Long userId, Integer originalMessageId) {
        rights(userId, originalMessageId);
        messageService.editText(userId, originalMessageId,
                "Отправьте ID группы, которую хотите удалить.");
        adminConversationService.waitForRemoveGroupData(userId);
    }

    private void handleAdminRemoveUserCallback(Long userId, Integer originalMessageId) {
        rights(userId, originalMessageId);
        messageService.editText(userId, originalMessageId,
                "Отправьте `groupChatId userId` (два числа через пробел).");
        adminConversationService.waitForRemoveUserData(userId);
    }

    private void rights(Long userId, Integer originalMessageId) {
        if (!messageService.isAdmin(userId)) {
            messageService.editText(userId, originalMessageId, "У вас нет прав админа.");
        }
    }


}
