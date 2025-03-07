package com.example.address_book.service;

import com.example.address_book.model.AddressBook;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AddressBookService {

    private final List<AddressBook> addressBookList = new ArrayList<>();
    private Long idCounter = 1L;

    public List<AddressBook> getAllContacts() {
        return addressBookList;
    }

    public Optional<AddressBook> getContactById(Long id) {
        return addressBookList.stream()
                .filter(contact -> contact.getId().equals(id))
                .findFirst();
    }

    public AddressBook createContact(AddressBook contact) {
        contact.setId(idCounter++);
        addressBookList.add(contact);
        return contact;
    }

    public Optional<AddressBook> updateContact(Long id, AddressBook updatedContact) {
        return getContactById(id).map(existingContact -> {
            existingContact.setName(updatedContact.getName());
            existingContact.setPhone(updatedContact.getPhone());
            existingContact.setAddress(updatedContact.getAddress());
            return existingContact;
        });
    }

    public boolean deleteContact(Long id) {
        return addressBookList.removeIf(contact -> contact.getId().equals(id));
    }
}
