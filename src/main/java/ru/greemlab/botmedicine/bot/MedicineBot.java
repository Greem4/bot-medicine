package ru.greemlab.botmedicine.bot;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.greemlab.botmedicine.dto.MedicineResponse.MedicineViewList;
import ru.greemlab.botmedicine.service.MedicineService;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Component
@RequiredArgsConstructor
public class MedicineBot extends TelegramLongPollingBot {

    @Value("${app.bot.name}")
    private String botUsername;

    @Value("${app.bot.token}")
    private String botToken;

    private final MedicineService medicineService;

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();

            if ("CHECK_RED".equalsIgnoreCase(callbackData)) {
                medicineService.getRedMedicines()
                        .collectList()
                        .subscribe(
                                redList -> {
                                    if (redList.isEmpty()) {
                                        editText(chatId, messageId, "✅ Нет просроченных лекарств.");
                                    } else {
                                        StringBuilder sb = new StringBuilder("⏰ Сроки годности менее 30 дней:\n\n");
                                        for (MedicineViewList m : redList) {
                                            sb.append("⚠")
                                                    .append(m.name())
                                                    .append(" (").append(m.serialNumber()).append(")\n     срок годности до:  ")
                                                    .append(m.expirationDate())
                                                    .append("\n");
                                        }
                                        editText(chatId, messageId, sb.toString());
                                    }
                                },
                                error -> {
                                    log.error("Ошибка при получении списка RED: {}", error.toString());
                                    editText(chatId, messageId, "❗ Произошла ошибка при запросе к серверу.");
                                }
                        );
            }
            return;
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            switch (text.toLowerCase()) {
                case "/start" -> {
                    String welcome = "👋 Привет! Я бот для проверки лекарств 💊. " +
                                     "Нажмите кнопку, чтобы посмотреть ⏰ сроки годности.";
                    sendTextWithInlineButton(chatId, welcome);
                }
                case "/help" -> {
                    String help = """
                            📜 Команды:
                            
                            /start – начать работу
                            /help – помощь
                            
                            используйте /start.""";
                    sendText(chatId, help);
                }
                default -> sendText(chatId, "❓ Неизвестная команда. Попробуйте /help.");
            }
        }
    }

    private void sendText(Long chatId, String text) {
        try {
            execute(SendMessage.builder()
                    .chatId(chatId.toString())
                    .text(text)
                    .build());
        } catch (Exception e) {
            log.error("Ошибка при отправке сообщения: {}", e.toString());
        }
    }

    private void sendTextWithInlineButton(Long chatId, String text) {
        InlineKeyboardButton button = new InlineKeyboardButton("⏰ Показать сроки годности");
        button.setCallbackData("CHECK_RED");

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(button));

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(rows);

        try {
            execute(SendMessage.builder()
                    .chatId(chatId.toString())
                    .text(text)
                    .replyMarkup(markup)
                    .build());
        } catch (Exception e) {
            log.error("Ошибка при отправке inline-кнопки: {}", e.toString());
        }
    }

    private void editText(Long chatId, Integer messageId, String newText) {
        try {
            execute(EditMessageText.builder()
                    .chatId(chatId.toString())
                    .messageId(messageId)
                    .text(newText)
                    .build());
        } catch (Exception e) {
            log.error("Ошибка при редактировании сообщения: {}", e.toString());
        }
    }
}
