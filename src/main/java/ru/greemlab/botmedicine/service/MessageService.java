package ru.greemlab.botmedicine.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.greemlab.botmedicine.telegram.TelegramExecutor;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final TelegramExecutor execute;

    public void sendText(Long chatId, String text) {
        try {
            var msg = SendMessage.builder()
                    .chatId(chatId.toString())
                    .text(text)
                    .parseMode("Markdown")
                    .build();
            execute.callApi(msg);
        } catch (Exception e) {
            log.error("Ошибка при отправке сообщения: {}", e.toString());
        }
    }

    public void sendTextWithInLineButton(Long chatId, String text,
                                         String buttonText, String callbackData) {
        var button = new InlineKeyboardButton(buttonText);
        button.setCallbackData(callbackData);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(button));

        var markup = new InlineKeyboardMarkup(rows);

        try {
            var msg = SendMessage.builder()
                    .chatId(chatId.toString())
                    .text(text)
                    .replyMarkup(markup)
                    .parseMode("Markdown")
                    .build();
            execute.callApi(msg);
        } catch (Exception e) {
            log.error("Ошибка при отправке inline-кнопки: {}", e.toString());
        }
    }

    public void editText(Long chatId, Integer messageId, String newText) {
        try {
            var edit = EditMessageText.builder()
                    .chatId(chatId.toString())
                    .messageId(messageId)
                    .text(newText)
                    .parseMode("MarkdownV2")
                    .build();
            execute.callApi(edit);
        } catch (Exception e) {
            log.error("Ошибка при редактировании сообщения: {}", e.toString());
        }
    }

    public void answerCallbackQuery(String callbackQueryId, String text, boolean showAlert) {
        try {
            var answer = AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQueryId)
                    .text(text)
                    .showAlert(showAlert)
                    .build();
            execute.callApi(answer);
        } catch (Exception e) {
            log.error("Ошибка при отправке ответа на callback query: {}", e.toString());
        }
    }

    public String escapeMarkdownV2(String text) {
        return text.replaceAll("([_*\\[\\]()~`>#+=|{}.!-])", "\\\\$1");
    }
}
