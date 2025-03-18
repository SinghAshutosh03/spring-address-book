//package com.example.address_book.service;
//
//import com.example.address_book.model.User;
//import com.example.address_book.repository.UserRepository;
//import com.example.address_book.util.JwtUtil;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.util.Optional;
//
//@Service
//public class AuthService {
//
//    private UserRepository userRepository;
//    private PasswordEncoder passwordEncoder;
//    private JwtUtil jwtUtil;
//
//    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
//        this.userRepository = userRepository;
//        this.passwordEncoder = new BCryptPasswordEncoder();
//        this.jwtUtil = jwtUtil;
//    }
//
//    public String loginUser(String username, String password) {
//        Optional<User> userOptional = userRepository.findByUsername(username);
//
//        if (userOptional.isPresent()) {
//            User user = userOptional.get();
//            if (passwordEncoder.matches(password, user.getPassword())) {
//                return jwtUtil.generateToken(username); // ✅ Return JWT on success
//            }
//        }
//        throw new RuntimeException("Invalid username or password!");
//    }
//}

package com.example.address_book.service;

import com.example.address_book.dto.UserDTO;
import com.example.address_book.model.User;
import com.example.address_book.repository.UserRepository;
import com.example.address_book.util.JwtUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtUtil jwtUtil;
    private JavaMailSender mailSender;
    private RabbitMQPublisher rabbitMQPublisher; // ✅ RabbitMQ Publisher Added

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, JavaMailSender mailSender, RabbitMQPublisher rabbitMQPublisher) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtUtil = jwtUtil;
        this.mailSender = mailSender;
        this.rabbitMQPublisher = rabbitMQPublisher;
    }

    public User registerUser(UserDTO userDTO) {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new RuntimeException("Username already taken!");
        }

        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Email already in use!");
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        User savedUser = userRepository.save(user);

        // 📩 Publish event when a new user registers
        rabbitMQPublisher.sendMessage("user.registration.queue", "New User Registered: " + userDTO.getUsername());

        return savedUser;
    }

    public String loginUser(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return jwtUtil.generateToken(username);
            }
        }
        throw new RuntimeException("Invalid username or password!");
    }

    public void forgotPassword(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("No user found with this email!");
        }

        User user = userOptional.get();
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setTokenExpiry(LocalDateTime.now().plusHours(1)); // ✅ Expire in 1 hour

        userRepository.save(user);

        // ✅ Publish event when a password reset request is made
        rabbitMQPublisher.sendMessage("password.reset.queue", "Password Reset Requested for: " + email);

        sendResetEmail(email, resetToken); // ✅ Send token via email
    }

    private void sendResetEmail(String email, String resetToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setTo(email);
            helper.setSubject("Password Reset Request");
            helper.setText("Use this token to reset your password: " + resetToken, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email!");
        }
    }

    public void resetPassword(String token, String newPassword) {
        Optional<User> userOptional = userRepository.findByResetToken(token);

        if (userOptional.isEmpty()) {
            throw new RuntimeException("Invalid or expired token!");
        }

        User user = userOptional.get();

        if (user.getTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token has expired!");
        }

        user.setPassword(passwordEncoder.encode(newPassword)); // ✅ Hash new password
        user.setResetToken(null); // ✅ Remove token after use
        user.setTokenExpiry(null);

        userRepository.save(user);

        // ✅ Publish event when password is successfully reset
        rabbitMQPublisher.sendMessage("password.reset.success.queue", "Password Reset Successful for: " + user.getEmail());
    }
}
