package com.company.request_approval.controller;

import com.company.request_approval.dto.UserDetailsDto;
import com.company.request_approval.security.JwtTokenProvider;
import com.company.request_approval.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public UserController(UserService userService, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDetailsDto> getCurrentUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String email = jwtTokenProvider.getUsernameFromJWT(token);
        UserDetailsDto userDetails = userService.getUserDetailsByEmail(email);
        return ResponseEntity.ok(userDetails);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDetailsDto> getUserById(@PathVariable Long id) {
        UserDetailsDto userDetails = userService.getUserDetails(id);
        return ResponseEntity.ok(userDetails);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<UserDetailsDto>> getAllUsers() {
        List<UserDetailsDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/subordinates")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<UserDetailsDto>> getSubordinates(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String email = jwtTokenProvider.getUsernameFromJWT(token);
        UserDetailsDto manager = userService.getUserDetailsByEmail(email);
        List<UserDetailsDto> subordinates = userService.getSubordinates(manager.getId());
        return ResponseEntity.ok(subordinates);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.getUserDetails(#id).getId() == principal.id")
    public ResponseEntity<UserDetailsDto> updateUser(@PathVariable Long id, @RequestBody UserDetailsDto userDetailsDto) {
        UserDetailsDto updatedUser = userService.updateUserDetails(id, userDetailsDto);
        return ResponseEntity.ok(updatedUser);
    }
    
    @PutMapping("/{id}/availability")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER') and #id == authentication.principal.id)")
    public ResponseEntity<UserDetailsDto> updateUserAvailability(
            @PathVariable Long id, 
            @RequestBody Map<String, Boolean> availabilityStatus) {
        
        Boolean isAvailable = availabilityStatus.get("isAvailable");
        if (isAvailable == null) {
            return ResponseEntity.badRequest().build();
        }
        
        UserDetailsDto updatedUser = userService.updateUserAvailability(id, isAvailable);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "User deleted successfully");
        return ResponseEntity.ok(response);
    }
}