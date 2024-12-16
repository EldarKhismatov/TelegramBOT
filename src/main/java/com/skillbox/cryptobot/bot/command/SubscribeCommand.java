package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.service.CryptoCurrencyService;
import com.skillbox.cryptobot.service.SubscriptionService;
import com.skillbox.cryptobot.utils.TextUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

/**
 * Обработка команды подписки на курс валюты
 */
@Service
@Slf4j
@AllArgsConstructor
public class SubscribeCommand implements IBotCommand {

    private final SubscriptionService subscriptionService;
    private final CryptoCurrencyService cryptoCurrencyService;

    @Override
    public String getCommandIdentifier() {
        return "subscribe";
    }

    @Override
    public String getDescription() {
        return "Подписывает пользователя на стоимость биткоина";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        Long chatId = message.getChatId();
        SendMessage response = new SendMessage();
        response.setChatId(chatId);

        try {
            if (arguments.length == 1 && arguments[0].matches("\\d+(\\.\\d+)?")) {
                double price = Double.parseDouble(arguments[0]);
                subscriptionService.subscribe(chatId, price);

                double currentPrice = cryptoCurrencyService.getBitcoinPrice();
                response.setText("Новая подписка создана на стоимость " + price + " USD\n" +
                        "Текущая стоимость биткоина: " + TextUtil.toString(currentPrice) + " USD");
            } else {
                response.setText("Введите корректное число. Пример: /subscribe 34600.00");
            }
            absSender.execute(response);
        } catch (Exception e) {
            log.error("Ошибка обработки команды /subscribe", e);
        }
    }
}
