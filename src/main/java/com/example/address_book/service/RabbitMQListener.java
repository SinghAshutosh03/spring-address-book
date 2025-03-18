package com.example.address_book.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQListener {

//    @RabbitListener(queues = "user.registration.queue")
//    public void receiveUserRegistrationMessage(String message) {
//        System.out.println("📥 Received User Registration Event: " + message);
//    }

    @RabbitListener(queues = "addressbook.contact.queue")
    public void receiveContactAddedMessage(String message) {
        System.out.println("📥 Received Contact Added Event: " + message);
    }
}
