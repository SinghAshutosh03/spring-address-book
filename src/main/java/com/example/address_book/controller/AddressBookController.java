package com.example.address_book.controller;

import com.example.address_book.model.AddressBook;
import com.example.address_book.service.AddressBookService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AddressBookController {

    private final AddressBookService service;

    public AddressBookController(AddressBookService service) {
        this.service = service;
    }

    @GetMapping("/")
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("Welcome to the Address Book App!");
    }

    @GetMapping("/all")
    public ResponseEntity<List<AddressBook>> getAllContacts() {
        return ResponseEntity.ok(service.getAllContacts());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<AddressBook> getContact(@PathVariable Long id) {
        return service.getContactById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/create")
    public ResponseEntity<AddressBook> createContact(@RequestBody AddressBook contact) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createContact(contact));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<AddressBook> updateContact(@PathVariable Long id, @RequestBody AddressBook contact) {
        return service.updateContact(id, contact)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        return service.deleteContact(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
