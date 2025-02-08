package ru.greemlab.botmedicine.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.greemlab.botmedicine.bot.MedicineBot;
import ru.greemlab.botmedicine.service.MedicineService;

@Configuration
public class BotConfig {

    @Bean
    public MedicineBot medicineBot(
            @Value("${app.bot.mame}") String name,
            @Value("${app.bot.token}") String token,
            MedicineService medicineService
    )  {
       return new MedicineBot(name, token, medicineService);
    }

    @Bean
    public TelegramBotsApi telegramBotsApi(MedicineBot bot) throws TelegramApiException {
        var api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(bot);
        return api;
    }
}
