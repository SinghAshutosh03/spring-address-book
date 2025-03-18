package com.example.address_book;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching // ✅ Enables caching
public class AddressBookApplication {
	public static void main(String[] args) {
		SpringApplication.run(AddressBookApplication.class, args);
	}
}
