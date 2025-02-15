package ru.greemlab.botmedicine.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
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

    @Value("${app.bot.admin-id}")
    private Long adminId;

    public boolean isAdmin(Long userId) {
        return adminId.equals(userId);
    }

    public Message sendMainMenu(Long chatId, String text, boolean isAdmin) {
        var btnCheckRed = InlineKeyboardButton.builder()
                .text("⏰ Срок годности")
                .callbackData("CHECK_RED")
                .build();
        var btnViewSchedule = InlineKeyboardButton.builder()
                .text("🗓 График")
                .callbackData("VIEW_SCHEDULE")
                .build();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(List.of(btnCheckRed, btnViewSchedule));

        if (isAdmin) {
            var btnAdmin = InlineKeyboardButton.builder()
                    .text("⚙ Админ-меню")
                    .callbackData("ADMIN_MENU")
                    .build();
            rows.add(List.of(btnAdmin));
        }

        var markup = new InlineKeyboardMarkup(rows);

        var msg = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("Markdown")
                .replyMarkup(markup)
                .build();
        return executeCall(msg);
    }

    public void editTextToAdminMenu(Long chatId, Integer messageId, String text) {
        var btnSetGroup = InlineKeyboardButton.builder()
                .text("Добавить/Обновить группу")
                .callbackData("ADMIN_SET_GROUP")
                .build();
        var btnRemoveGroup = InlineKeyboardButton.builder()
                .text("Удалить группу")
                .callbackData("ADMIN_REMOVE_GROUP")
                .build();
        var btnRemoveUser = InlineKeyboardButton.builder()
                .text("Удалить пользователя")
                .callbackData("ADMIN_REMOVE_USER")
                .build();

        var rows = List.of(
                List.of(btnSetGroup),
                List.of(btnRemoveGroup),
                List.of(btnRemoveUser)
        );
        var markup = new InlineKeyboardMarkup(rows);

        var edit = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("Markdown")
                .replyMarkup(markup)
                .build();

        try {
            execute.callApi(edit);
        } catch (Exception e) {
            log.error("Ошибка при редактировании на админ-меню: {}", e.getMessage());
        }
    }


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

    public void sendPhoto(Long chatId, String photoUrl, String caption) {
        var sendPhoto = new SendPhoto();
        sendPhoto.setChatId(String.valueOf(chatId));
        sendPhoto.setPhoto(new InputFile(photoUrl));
        sendPhoto.setCaption(caption);

        try {
            execute.callApiPhoto(sendPhoto);
        } catch (RuntimeException e) {
            log.error("Ошибка отправки : ", e);
        }
    }

    public void editText(Long chatId, Integer messageId, String newText) {
        try {
            var edit = EditMessageText.builder()
                    .chatId(chatId.toString())
                    .messageId(messageId)
                    .text(newText)
                    .parseMode("Markdown")
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

    private Message executeCall(SendMessage msg) {
        try {
            return execute.callApi(msg);
        } catch (Exception e) {
            log.error("Ошибка при отправке сообщения: ", e);
        }
        return null;
    }
}
