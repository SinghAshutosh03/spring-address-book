package com.example.address_book.repository;

import com.example.address_book.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email); // ✅ Now we can look up users by email
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    Optional<User> findByResetToken(String token);

//    Optional<User> findByResetToken(String token);
}
