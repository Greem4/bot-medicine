package ru.greemlab.botmedicine.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.greemlab.botmedicine.telegram.TelegramExecutor;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final TelegramExecutor execute;

    public Message sendText(Long chatId, String text) {
        try {
            var msg = SendMessage.builder()
                    .chatId(chatId.toString())
                    .text(text)
                    .parseMode("Markdown")
                    .build();
            return execute.callApi(msg);
        } catch (Exception e) {
            log.error("Ошибка при отправке сообщения: {}", e.toString());
            return null;
        }
    }

    public Message sendPhoto(Long chatId, String photoUrl, String caption) {
        var sendPhoto = new SendPhoto();
        sendPhoto.setChatId(String.valueOf(chatId));
        sendPhoto.setPhoto(new InputFile(photoUrl));
        sendPhoto.setCaption(caption);

        try {
            return execute.callApiPhoto(sendPhoto);
        } catch (RuntimeException e) {
            log.error("Ошибка отправки : ", e);
            return null;
        }
    }

    public Message sendTextWithTwoInlineButtons(Long chatId, String text,
                                               String button1Text, String button1Callback,
                                               String button2Text, String button2Callback) {

        var btn1 = InlineKeyboardButton.builder()
                .text(button1Text)
                .callbackData(button1Callback)
                .build();
        var btn2 = InlineKeyboardButton.builder()
                .text(button2Text)
                .callbackData(button2Callback)
                .build();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(btn1, btn2));
        var markup = new InlineKeyboardMarkup(rows);

        try {
            var msg = SendMessage.builder()
                    .chatId(chatId.toString())
                    .text(text)
                    .replyMarkup(markup)
                    .parseMode("Markdown")
                    .build();
            return execute.callApi(msg);
        } catch (Exception e) {
            log.error("Ошибка при отправке inline-кнопок: {}", e.toString());
            return null;
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
