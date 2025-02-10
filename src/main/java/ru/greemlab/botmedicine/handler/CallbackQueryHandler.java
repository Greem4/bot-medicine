package ru.greemlab.botmedicine.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import ru.greemlab.botmedicine.dto.MedicineResponse;
import ru.greemlab.botmedicine.service.MedicineService;
import ru.greemlab.botmedicine.service.MessageService;

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
        var callbackData = callbackQuery.getData();
        var callbackQueryId = callbackQuery.getId();
        var message = callbackQuery.getMessage();
        var chatId = message.getChatId();
        var messageId = message.getMessageId();

        messageService.answerCallbackQuery(callbackQueryId, PROCESSING_MESSAGE, false);

        if (CALLBACK_CHECK_RED.equalsIgnoreCase(callbackData)) {
            handleCheckRedCallback(chatId, messageId);
        } else {
            log.warn("Неизвестное значение callback: {}", callbackData);
            messageService.editText(chatId, messageId, UNKNOWN_CALLBACK_MESSAGE);
        }
    }

    private void handleCheckRedCallback(Long chatId, Integer messageId) {
        medicineService.getRedMedicines()
                .collectList()
                .subscribe(
                        redList -> {
                            if (redList.isEmpty()) {
                                messageService.editText(chatId, messageId, NO_MEDICINES_MESSAGE);
                            } else {
                                var formattedMedicines = redList.stream()
                                        .map(this::formatMedicine)
                                        .collect(Collectors.joining());
                                var result = HEADER + formattedMedicines;
                                messageService.editText(chatId, messageId, result);
                            }
                        },
                        error -> {
                            log.error("Ошибка при получении списка лекарств", error);
                            messageService.editText(chatId, messageId, ERROR_MESSAGE);
                        }
                );
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
