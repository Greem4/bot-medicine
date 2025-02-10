package ru.greemlab.botmedicine.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.greemlab.botmedicine.telegram.TelegramExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class MessageServiceTest {

    private TelegramExecutor telegramExecutor;
    private MessageService messageService;

    @BeforeEach
    void setUp() {
        telegramExecutor = mock(TelegramExecutor.class);
        messageService = new MessageService(telegramExecutor);
    }

    @Test
    void testSendText() {
        Long chatId = 123L;
        String text = "Hello, world!";
        Message fakeMessage = new Message();

        when(telegramExecutor.callApi(any(SendMessage.class))).thenReturn(fakeMessage);

        Message returnedMessage = messageService.sendText(chatId, text);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramExecutor).callApi(captor.capture());
        SendMessage sent = captor.getValue();

        assertEquals(chatId.toString(), sent.getChatId());
        assertEquals(text, sent.getText());
        assertEquals("Markdown", sent.getParseMode());
        assertEquals(fakeMessage, returnedMessage);
    }
}
