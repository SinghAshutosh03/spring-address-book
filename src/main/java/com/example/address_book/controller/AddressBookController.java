package com.example.address_book.controller;

import com.example.address_book.model.AddressBook;
import com.example.address_book.repository.AddressBookRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AddressBookController {

    private final AddressBookRepository repository;

    public AddressBookController(AddressBookRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/")
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("Welcome to the Address Book App!");
    }

    @GetMapping("/all")
    public ResponseEntity<List<AddressBook>> getAllContacts() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<AddressBook> getContact(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/create")
    public ResponseEntity<AddressBook> createContact(@RequestBody AddressBook contact) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(contact));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<AddressBook> updateContact(@PathVariable Long id, @RequestBody AddressBook contact) {
        return repository.findById(id)
                .map(existingContact -> {
                    existingContact.setName(contact.getName());
                    existingContact.setPhone(contact.getPhone());
                    existingContact.setAddress(contact.getAddress());
                    return ResponseEntity.ok(repository.save(existingContact));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
