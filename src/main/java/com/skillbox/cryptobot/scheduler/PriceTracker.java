package com.skillbox.cryptobot.scheduler;

import com.skillbox.cryptobot.bot.CryptoBot;
import com.skillbox.cryptobot.entity.Subscriber;
import com.skillbox.cryptobot.repository.SubscriberRepository;
import com.skillbox.cryptobot.service.CryptoCurrencyService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Добавьте этот импорт
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.concurrent.ConcurrentHashMap;

@Component
@AllArgsConstructor
@Slf4j // Добавлено для логгирования
public class PriceTracker {

    private final CryptoCurrencyService currencyService;
    private final SubscriberRepository repository;
    private final AbsSender bot;

    private final ConcurrentHashMap<Long, Long> lastNotificationTime = new ConcurrentHashMap<>();

    @Scheduled(fixedRate = 120000) // Каждые 2 минуты
    public void checkPrice() {
        double currentPrice;
        try {
            currentPrice = currencyService.getBitcoinPrice();
            repository.findAll().forEach(subscriber -> {
                if (subscriber.getSubscribedPrice() != null && subscriber.getSubscribedPrice() >= currentPrice) {
                    long now = System.currentTimeMillis();
                    long lastTime = lastNotificationTime.getOrDefault(subscriber.getTelegramId(), 0L);
                    if (now - lastTime > 10 * 60 * 1000) { // 10 минут
                        sendNotification(subscriber, currentPrice);
                        lastNotificationTime.put(subscriber.getTelegramId(), now);
                    }
                }
            });
        } catch (Exception e) {
            log.error("Ошибка при получении цены биткоина", e);
        }
    }

    private void sendNotification(Subscriber subscriber, double price) {
        SendMessage message = new SendMessage();
        message.setChatId(subscriber.getTelegramId());
        message.setText("Пора покупать! Стоимость биткоина: " + price + " USD");
        try {
            bot.execute(message);
        } catch (Exception e) {
            log.error("Ошибка при отправке уведомления пользователю " + subscriber.getTelegramId(), e);
        }
    }

    @Bean
    public AbsSender absSender(CryptoBot cryptoBot) {
        return cryptoBot;
    }
}
