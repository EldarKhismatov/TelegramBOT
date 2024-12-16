package com.skillbox.cryptobot.service;

import com.skillbox.cryptobot.entity.Subscriber;
import com.skillbox.cryptobot.repository.SubscriberRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class SubscriptionService {

    private final SubscriberRepository repository;

    public void subscribe(Long telegramId, Double price) {
        Optional<Subscriber> subscriberOpt = repository.findByTelegramId(telegramId);
        Subscriber subscriber = subscriberOpt.orElseGet(() -> {
            Subscriber newSub = new Subscriber();
            newSub.setTelegramId(telegramId);
            return newSub;
        });
        subscriber.setSubscribedPrice(price);
        repository.save(subscriber);
    }

    public Optional<Double> getSubscription(Long telegramId) {
        return repository.findByTelegramId(telegramId).map(Subscriber::getSubscribedPrice);
    }

    public void unsubscribe(Long telegramId) {
        repository.findByTelegramId(telegramId).ifPresent(subscriber -> {
            subscriber.setSubscribedPrice(null);
            repository.save(subscriber);
        });
    }
}