//package com.example.address_book.service;
//
//import com.example.address_book.dto.AddressBookDTO;
//import com.example.address_book.model.AddressBook;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Service
//public class AddressBookService {
//
//    private final List<AddressBook> addressBookList = new ArrayList<>();
//    private Long idCounter = 1L;
//
//    public List<AddressBookDTO> getAllContacts() {
//        return addressBookList.stream()
//                .map(contact -> new AddressBookDTO(contact.getName(), contact.getPhone(), contact.getAddress()))
//                .collect(Collectors.toList());
//    }
//
//    public Optional<AddressBookDTO> getContactById(Long id) {
//        return addressBookList.stream()
//                .filter(contact -> contact.getId().equals(id))
//                .map(contact -> new AddressBookDTO(contact.getName(), contact.getPhone(), contact.getAddress()))
//                .findFirst();
//    }
//
//    public AddressBookDTO createContact(AddressBookDTO contactDTO) {
//        AddressBook contact = new AddressBook(contactDTO.getName(), contactDTO.getPhone(), contactDTO.getAddress());
//        contact.setId(idCounter++);
//        addressBookList.add(contact);
//        return new AddressBookDTO(contact.getName(), contact.getPhone(), contact.getAddress());
//    }
//
//    public Optional<AddressBookDTO> updateContact(Long id, AddressBookDTO updatedContactDTO) {
//        return getContactById(id).map(existingContact -> {
//            existingContact.setName(updatedContactDTO.getName());
//            existingContact.setPhone(updatedContactDTO.getPhone());
//            existingContact.setAddress(updatedContactDTO.getAddress());
//            return new AddressBookDTO(existingContact.getName(), existingContact.getPhone(), existingContact.getAddress());
//        });
//    }
//
//    public boolean deleteContact(Long id) {
//        return addressBookList.removeIf(contact -> contact.getId().equals(id));
//    }
//}

package com.example.address_book.service;

import com.example.address_book.dto.AddressBookDTO;
import com.example.address_book.model.AddressBook;
import com.example.address_book.repository.AddressBookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AddressBookService {

    private final AddressBookRepository repository;

    public AddressBookService(AddressBookRepository repository) {
        this.repository = repository;
    }

    public List<AddressBookDTO> getAllContacts() {
        return repository.findAll().stream()
                .map(contact -> new AddressBookDTO(contact.getName(), contact.getPhone(), contact.getAddress()))
                .collect(Collectors.toList());
    }

    public Optional<AddressBookDTO> getContactById(Long id) {
        return repository.findById(id)
                .map(contact -> new AddressBookDTO(contact.getName(), contact.getPhone(), contact.getAddress()));
    }

    public AddressBookDTO createContact(AddressBookDTO contactDTO) {
        AddressBook contact = new AddressBook(contactDTO.getName(), contactDTO.getPhone(), contactDTO.getAddress());
        AddressBook savedContact = repository.save(contact);
        return new AddressBookDTO(savedContact.getName(), savedContact.getPhone(), savedContact.getAddress());
    }

    public Optional<AddressBookDTO> updateContact(Long id, AddressBookDTO updatedContactDTO) {
        return repository.findById(id).map(existingContact -> {
            existingContact.setName(updatedContactDTO.getName());
            existingContact.setPhone(updatedContactDTO.getPhone());
            existingContact.setAddress(updatedContactDTO.getAddress());
            AddressBook updatedContact = repository.save(existingContact);
            return new AddressBookDTO(updatedContact.getName(), updatedContact.getPhone(), updatedContact.getAddress());
        });
    }

    public boolean deleteContact(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }
}
