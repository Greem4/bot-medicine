package ru.greemlab.botmedicine.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import reactor.core.publisher.Flux;
import ru.greemlab.botmedicine.dto.MedicineResponse;
import ru.greemlab.botmedicine.dto.MedicineResponse.MedicineViewList;
import ru.greemlab.botmedicine.service.MedicineService;
import ru.greemlab.botmedicine.service.MessageService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;

class CallbackQueryHandlerTest {

    private MedicineService medicineService;
    private MessageService messageService;
    private CallbackQueryHandler callbackQueryHandler;

    private static final String CALLBACK_CHECK_RED = "CHECK_RED";
    private static final String NO_MEDICINES_MESSAGE = "✅ Нет просроченных лекарств.";
    private static final String UNKNOWN_CALLBACK_MESSAGE = "❓ Неизвестное действие.";
    private static final String PROCESSING_MESSAGE = "Запрос обрабатывается…";

    @BeforeEach
    void setUp() {
        medicineService = mock(MedicineService.class);
        messageService = mock(MessageService.class);
        callbackQueryHandler = new CallbackQueryHandler(medicineService, messageService);
        when(messageService.escapeMarkdownV2(anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void testHandleCallback_knownCallback_noMedicines() {
        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        Message message = mock(Message.class);
        when(callbackQuery.getData()).thenReturn(CALLBACK_CHECK_RED);
        when(callbackQuery.getId()).thenReturn("callback-id");
        when(callbackQuery.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(123L);
        when(message.getMessageId()).thenReturn(10);

        when(medicineService.getRedMedicines()).thenReturn(Flux.empty());

        callbackQueryHandler.handleCallback(callbackQuery);

        verify(messageService).answerCallbackQuery("callback-id", PROCESSING_MESSAGE, false);

        verify(messageService).editText(123L, 10, NO_MEDICINES_MESSAGE);
    }

    @Test
    void testHandleCallback_knownCallback_withMedicines() {
        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        Message message = mock(Message.class);

        when(callbackQuery.getData()).thenReturn(CALLBACK_CHECK_RED);
        when(callbackQuery.getId()).thenReturn("callback-id");
        when(callbackQuery.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(456L);
        when(message.getMessageId()).thenReturn(20);

        // Создаем тестовое лекарство, как в API
        MedicineViewList med = new MedicineResponse.MedicineViewList(
                8,
                "Каптоприл",
                "70422",
                "01-03-2025",
                "red"
        );

        when(medicineService.getRedMedicines()).thenReturn(Flux.just(med));

        callbackQueryHandler.handleCallback(callbackQuery);

        verify(messageService).answerCallbackQuery("callback-id", PROCESSING_MESSAGE, false);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(messageService).editText(eq(456L), eq(20), captor.capture());

        String resultText = captor.getValue();
        System.out.println("Фактический текст сообщения: " + resultText);

        assertThat(resultText, containsString("Каптоприл"));
        assertThat(resultText, containsString("70422"));
        assertThat(resultText, containsString("01-03-2025"));
    }


    @Test
    void testHandleCallback_unknownCallback() {
        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        Message message = mock(Message.class);
        when(callbackQuery.getData()).thenReturn("UNKNOWN_ACTION");
        when(callbackQuery.getId()).thenReturn("callback-id");
        when(callbackQuery.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(789L);
        when(message.getMessageId()).thenReturn(30);

        callbackQueryHandler.handleCallback(callbackQuery);

        verify(messageService).editText(789L, 30, UNKNOWN_CALLBACK_MESSAGE);
    }
}
