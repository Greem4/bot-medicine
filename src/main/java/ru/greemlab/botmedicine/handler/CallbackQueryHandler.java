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

    private final MedicineService medicineService;
    private final MessageService messageService;

    public void handleCallback(CallbackQuery callbackQuery) {
        var callbackData = callbackQuery.getData();
        var callbackQueryId = callbackQuery.getId();
        var chatId = callbackQuery.getMessage().getChatId();
        var messageId = callbackQuery.getMessage().getMessageId();

        messageService.answerCallbackQuery(callbackQueryId, "Запрос обрабатывается…", false);

        if ("CHECK_RED".equalsIgnoreCase(callbackData)) {
            medicineService.getRedMedicines()
                    .collectList()
                    .subscribe(
                            redList -> {
                                if (redList.isEmpty()) {
                                    messageService.editText(chatId, messageId, "✅ Нет просроченных лекарств.");
                                } else {
                                    var header = "*Сроки годности менее 30 дней:*\n\n";
                                    var formattedMedicines = redList.stream()
                                            .map(this::formatMedicine)
                                            .collect(Collectors.joining(""));
                                    var result = header + formattedMedicines;
                                    messageService.editText(chatId, messageId, result);
                                }
                            },
                            error -> {
                                log.error("Ошибка при получении списка лекарств: {}", error.toString());
                                messageService.editText(chatId, messageId, "❗ Произошла ошибка при запросе к серверу.");
                            }
                    );
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
