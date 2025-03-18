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

//package com.example.address_book.service;
//
//import com.example.address_book.dto.AddressBookDTO;
//import com.example.address_book.model.AddressBook;
//import com.example.address_book.repository.AddressBookRepository;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Service
//public class AddressBookService {
//
//    private final AddressBookRepository repository;
//
//    public AddressBookService(AddressBookRepository repository) {
//        this.repository = repository;
//    }
//
//    public List<AddressBookDTO> getAllContacts() {
//        return repository.findAll().stream()
//                .map(contact -> new AddressBookDTO(contact.getName(), contact.getPhone(), contact.getAddress()))
//                .collect(Collectors.toList());
//    }
//
//    public Optional<AddressBookDTO> getContactById(Long id) {
//        return repository.findById(id)
//                .map(contact -> new AddressBookDTO(contact.getName(), contact.getPhone(), contact.getAddress()));
//    }
//
//    public AddressBookDTO createContact(AddressBookDTO contactDTO) {
//        AddressBook contact = new AddressBook(contactDTO.getName(), contactDTO.getPhone(), contactDTO.getAddress());
//        AddressBook savedContact = repository.save(contact);
//        return new AddressBookDTO(savedContact.getName(), savedContact.getPhone(), savedContact.getAddress());
//    }
//
//    public Optional<AddressBookDTO> updateContact(Long id, AddressBookDTO updatedContactDTO) {
//        return repository.findById(id).map(existingContact -> {
//            existingContact.setName(updatedContactDTO.getName());
//            existingContact.setPhone(updatedContactDTO.getPhone());
//            existingContact.setAddress(updatedContactDTO.getAddress());
//            AddressBook updatedContact = repository.save(existingContact);
//            return new AddressBookDTO(updatedContact.getName(), updatedContact.getPhone(), updatedContact.getAddress());
//        });
//    }
//
//    public boolean deleteContact(Long id) {
//        if (repository.existsById(id)) {
//            repository.deleteById(id);
//            return true;
//        }
//        return false;
//    }
//}
package com.example.address_book.service;

import com.example.address_book.dto.AddressBookDTO;
import com.example.address_book.model.AddressBook;
import com.example.address_book.repository.AddressBookRepository;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AddressBookService implements IAddressBookService {

    private AddressBookRepository repository;
    private ModelMapper modelMapper;
    private RabbitMQPublisher rabbitMQPublisher;

    public AddressBookService(AddressBookRepository repository, ModelMapper modelMapper, RabbitMQPublisher rabbitMQPublisher) {
        this.repository = repository;
        this.modelMapper = modelMapper;
        this.rabbitMQPublisher = rabbitMQPublisher;
    }

    @Override
    @Cacheable(value = "contacts") // ✅ Cache the list of contacts
    public List<AddressBookDTO> getAllContacts() {
        return repository.findAll().stream()
                .map(contact -> modelMapper.map(contact, AddressBookDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "contact", key = "#id") // ✅ Cache a single contact
    public AddressBookDTO getContactById(Long id) {
        Optional<AddressBook> contact = repository.findById(id);
        return contact.map(c -> modelMapper.map(c, AddressBookDTO.class)).orElse(null);
    }

    @Override
    @CacheEvict(value = {"contacts", "contact"}, allEntries = true) // ✅ Clear cache when adding
    public AddressBookDTO createContact(AddressBookDTO contactDTO) {
        AddressBook contact = modelMapper.map(contactDTO, AddressBook.class);
        AddressBook savedContact = repository.save(contact);

        // 📩 Publish event when a new contact is added
        rabbitMQPublisher.sendMessage("contact.added.queue", "New Contact Added: " + contactDTO.getName());

        return modelMapper.map(savedContact, AddressBookDTO.class);
    }

    @Override
    @CacheEvict(value = {"contacts", "contact"}, allEntries = true) // ✅ Clear cache when updating
    public AddressBookDTO updateContact(Long id, AddressBookDTO contactDTO) {
        if (repository.existsById(id)) {
            AddressBook contact = modelMapper.map(contactDTO, AddressBook.class);
            contact.setId(id);
            AddressBook updatedContact = repository.save(contact);

            // 📩 Publish event when a contact is updated
            rabbitMQPublisher.sendMessage("contact.updated.queue", "Contact Updated: " + contactDTO.getName());

            return modelMapper.map(updatedContact, AddressBookDTO.class);
        }
        return null;
    }

    @Override
    @CacheEvict(value = {"contacts", "contact"}, allEntries = true) // ✅ Clear cache when deleting
    public void deleteContact(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);

            // 📩 Publish event when a contact is deleted
            rabbitMQPublisher.sendMessage("contact.deleted.queue", "Contact Deleted with ID: " + id);
        }
    }
}

