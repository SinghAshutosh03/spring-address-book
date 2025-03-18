package com.example.address_book.service;

import com.example.address_book.dto.AddressBookDTO;
import java.util.List;

public interface IAddressBookService {
    List<AddressBookDTO> getAllContacts();
    AddressBookDTO getContactById(Long id);
    AddressBookDTO createContact(AddressBookDTO contactDTO);
    AddressBookDTO updateContact(Long id, AddressBookDTO contactDTO);
    void deleteContact(Long id);
}
