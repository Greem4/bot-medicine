package ru.greemlab.botmedicine.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.greemlab.botmedicine.dto.MedicineResponse;
import ru.greemlab.botmedicine.service.MedicineService;
import ru.greemlab.botmedicine.service.MessageService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CallbackQueryHandler {
    private static final String CALLBACK_CHECK_RED = "CHECK_RED";
    private static final String PROCESSING_MESSAGE = "Запрос обрабатывается…";
    private static final String NO_MEDICINES_MESSAGE = "✅ Нет просроченных лекарств.";
    private static final String ERROR_MESSAGE = "❗ Произошла ошибка при запросе к серверу.";
    private static final String UNKNOWN_CALLBACK_MESSAGE = "❓ Неизвестное действие.";
    private static final String HEADER = "*Сроки годности менее 30 дней:*\n\n";

    private final MedicineService medicineService;
    private final MessageService messageService;

    public void handleCallback(CallbackQuery callbackQuery) {
        var userId = callbackQuery.getFrom().getId();
        var messageId = callbackQuery.getMessage().getMessageId();
        var callbackData = callbackQuery.getData();
        var callbackQueryId = callbackQuery.getId();

        messageService.answerCallbackQuery(callbackQueryId, PROCESSING_MESSAGE, false);

        if (CALLBACK_CHECK_RED.equalsIgnoreCase(callbackData)) {
            handleCheckRedCallback(userId, messageId);
        } else {
            log.warn("Неизвестное значение callback: {}", callbackData);
            messageService.sendText(userId, UNKNOWN_CALLBACK_MESSAGE);
        }
    }

    private void handleCheckRedCallback(Long chatId, Integer messageId) {
        try {
            List<MedicineResponse.MedicineViewList> redList = medicineService.getRedMedicines();
            if (redList == null || redList.isEmpty()) {
                messageService.editText(chatId, messageId, NO_MEDICINES_MESSAGE);
            } else {
                String formatted = redList.stream()
                        .map(this::formatMedicine)
                        .collect(Collectors.joining());
                messageService.editText(chatId, messageId, HEADER + formatted);
            }
        } catch (Exception e) {
            log.error("Ошибка при получении списка лекарств", e);
            messageService.editText(chatId, messageId, ERROR_MESSAGE);
        }
    }

    private String formatMedicine(MedicineResponse.MedicineViewList m) {
        return String.format("""
                        • *%s* \\(%s\\)
                          Срок годности до:
                          ➡*%s*
                        """,
                messageService.escapeMarkdownV2(m.name()),
                messageService.escapeMarkdownV2(m.serialNumber()),
                messageService.escapeMarkdownV2(m.expirationDate())
        );
    }
}
