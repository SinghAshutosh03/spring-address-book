package com.example.address_book.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AddressBookController {

    @GetMapping("/hello")
    public String sayHello() {
        return "Welcome to the Address Book App!";
    }
}