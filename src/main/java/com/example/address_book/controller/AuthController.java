//package com.example.address_book.controller;
//
//import com.example.address_book.service.AuthService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/auth")
//public class AuthController {
//
//    private AuthService authService;
//
//    public AuthController(AuthService authService) {
//        this.authService = authService;
//    }
//
//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
//        String username = request.get("username");
//        String password = request.get("password");
//
//        if (username == null || password == null) {
//            return ResponseEntity.badRequest().body("Username and Password are required!");
//        }
//
//        try {
//            String token = authService.loginUser(username, password);
//            return ResponseEntity.ok(Map.of("token", token));
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(401).body("Invalid username or password!");
//        }
//    }
//}


package com.example.address_book.controller;

import com.example.address_book.dto.UserDTO;
import com.example.address_book.model.User;
import com.example.address_book.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")  // ✅ Added Registration Endpoint
    public ResponseEntity<?> register(@RequestBody UserDTO userDTO) {
        if (userDTO.getUsername() == null || userDTO.getPassword() == null || userDTO.getEmail() == null) {
            return ResponseEntity.badRequest().body("Username, Email, and Password are required!");
        }

        try {
            User registeredUser = authService.registerUser(userDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body("Username and Password are required!");
        }

        try {
            String token = authService.loginUser(username, password);
            return ResponseEntity.ok(Map.of("token", token));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body("Invalid username or password!");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null) {
            return ResponseEntity.badRequest().body("Email is required!");
        }

        authService.forgotPassword(email);
        return ResponseEntity.ok("Password reset email sent!");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        if (token == null || newPassword == null) {
            return ResponseEntity.badRequest().body("Token and new password are required!");
        }

        authService.resetPassword(token, newPassword);
        return ResponseEntity.ok("Password reset successful!");
    }
}
