package com.company.request_approval.controller;

import com.company.request_approval.dto.LoginRequest;
import com.company.request_approval.dto.SignupRequest;
import com.company.request_approval.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        System.out.println("Login endpoint hit with email: " + loginRequest.getEmail());
        Map<String, Object> response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        Map<String, Object> response = authService.register(signupRequest);
        return ResponseEntity.ok(response);
    }
}