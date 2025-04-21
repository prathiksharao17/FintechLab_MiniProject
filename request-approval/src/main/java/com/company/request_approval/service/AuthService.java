package com.company.request_approval.service;

import com.company.request_approval.dto.LoginRequest;
import com.company.request_approval.dto.SignupRequest;
import com.company.request_approval.model.User;
import com.company.request_approval.model.UserDetail;
import com.company.request_approval.repository.UserDetailRepository;
import com.company.request_approval.repository.UserRepository;
import com.company.request_approval.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserDetailRepository userDetailRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    
    @Value("${app.defaultAnnualLeaveAllowance}")
    private int defaultAnnualLeaveAllowance;

    public AuthService(UserRepository userRepository, 
                       UserDetailRepository userDetailRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.userDetailRepository = userDetailRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public Map<String, Object> login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtTokenProvider.generateToken(authentication);

            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + loginRequest.getEmail()));

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("userId", user.getId());
            response.put("email", user.getEmail());
            response.put("role", user.getRole());

            return response;

        } catch (Exception ex) {
            System.out.println("Login failed: " + ex.getMessage());
            throw new RuntimeException("Invalid email or password");
        }
    }

    @Transactional
    public Map<String, Object> register(SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new RuntimeException("Email is already taken!");
        }

        User user = new User();
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setRole(signupRequest.getRole() != null ? signupRequest.getRole() : "EMPLOYEE");
        
        if (signupRequest.getManagerId() != null) {
            User manager = userRepository.findById(signupRequest.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Manager not found with id: " + signupRequest.getManagerId()));
            user.setManager(manager);
        }
        
        User savedUser = userRepository.save(user);
        
        UserDetail userDetail = new UserDetail();
        userDetail.setUser(savedUser);
        userDetail.setFirstName(signupRequest.getFirstName());
        userDetail.setLastName(signupRequest.getLastName());
        userDetail.setDepartment(signupRequest.getDepartment());
        userDetail.setPosition(signupRequest.getPosition());
        userDetail.setPhoneNumber(signupRequest.getPhoneNumber());
        userDetail.setAnnualLeaveAllowance(defaultAnnualLeaveAllowance);
        userDetail.setRemainingLeaveAllowance(defaultAnnualLeaveAllowance);
        
        userDetailRepository.save(userDetail);
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", savedUser.getId());
        response.put("message", "User registered successfully!");
        
        return response;
    }
}