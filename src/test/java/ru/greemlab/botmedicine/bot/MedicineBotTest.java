package ru.greemlab.botmedicine.bot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.greemlab.botmedicine.handler.CallbackQueryHandler;
import ru.greemlab.botmedicine.handler.CommandHandler;

import static org.mockito.Mockito.*;

class MedicineBotTest {

    private MedicineBot medicineBot;
    private CommandHandler commandHandler;
    private CallbackQueryHandler callbackQueryHandler;

    @BeforeEach
    void setUp() {
        commandHandler = mock(CommandHandler.class);
        callbackQueryHandler = mock(CallbackQueryHandler.class);
        medicineBot = new MedicineBot(commandHandler, callbackQueryHandler);
    }

    @Test
    void testOnUpdateReceived_callbackQueryOnly() {
        Update update = mock(Update.class);
        CallbackQuery callbackQuery = mock(CallbackQuery.class);

        when(update.hasCallbackQuery()).thenReturn(true);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(update.hasMessage()).thenReturn(false);

        medicineBot.onUpdateReceived(update);

        verify(callbackQueryHandler, times(1)).handleCallback(callbackQuery);
        verifyNoInteractions(commandHandler);
    }

    @Test
    void testOnUpdateReceived_messageWithTextOnly() {
        Update update = mock(Update.class);
        Message message = mock(Message.class);

        when(update.hasCallbackQuery()).thenReturn(false);
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(true);

        medicineBot.onUpdateReceived(update);

        verify(commandHandler, times(1)).handleCommand(message);
        verifyNoInteractions(callbackQueryHandler);
    }

    @Test
    void testOnUpdateReceived_bothCallbackAndMessageWithText() {
        Update update = mock(Update.class);
        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        Message message = mock(Message.class);

        when(update.hasCallbackQuery()).thenReturn(true);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(true);

        medicineBot.onUpdateReceived(update);

        verify(callbackQueryHandler, times(1)).handleCallback(callbackQuery);
        verify(commandHandler, times(1)).handleCommand(message);
    }

    @Test
    void testOnUpdateReceived_noValidUpdate() {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        when(update.hasCallbackQuery()).thenReturn(false);
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(false);

        medicineBot.onUpdateReceived(update);

        verifyNoInteractions(callbackQueryHandler);
        verifyNoInteractions(commandHandler);
    }
}
