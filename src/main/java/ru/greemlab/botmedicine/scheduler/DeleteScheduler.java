package ru.greemlab.botmedicine.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessages;
import ru.greemlab.botmedicine.telegram.TelegramExecutor;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteScheduler {

    private final TelegramExecutor execute;

    public void schedulerDeleteMessage(Long chatId, Integer messageId, long delaySeconds) {
        var scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.schedule(() -> {
            try {
                var delete = DeleteMessages.builder()
                        .chatId(chatId.toString())
                        .messageId(messageId)
                        .build();
                execute.execute(delete);
            } catch (Exception e) {
                log.error("Ошибка при удалении сообщения: {}", e.toString());
            } finally {
                scheduler.shutdown();
            }
        }, delaySeconds, TimeUnit.SECONDS);
    }
}
