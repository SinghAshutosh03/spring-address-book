package com.example.address_book.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue userRegistrationQueue() {
        return new Queue("user.registration.queue", true);
    }

    @Bean
    public Queue contactAddedQueue() {
        return new Queue("addressbook.contact.queue", true);
    }
}
