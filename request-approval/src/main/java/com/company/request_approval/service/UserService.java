package com.company.request_approval.service;

import com.company.request_approval.dto.UserDetailsDto; 
import com.company.request_approval.model.User;
import com.company.request_approval.model.UserDetail;
import com.company.request_approval.repository.UserDetailRepository;
import com.company.request_approval.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserDetailRepository userDetailRepository;

    public UserService(UserRepository userRepository, UserDetailRepository userDetailRepository) {
        this.userRepository = userRepository;
        this.userDetailRepository = userDetailRepository;
    }

    @Transactional(readOnly = true)
    public UserDetailsDto getUserDetails(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        UserDetail userDetail = userDetailRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User detail not found for user id: " + userId));
        
        return mapToUserDetailsDto(user, userDetail);
    }

    @Transactional(readOnly = true)
    public UserDetailsDto getUserDetailsByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        UserDetail userDetail = userDetailRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("User detail not found for user id: " + user.getId()));
        
        return mapToUserDetailsDto(user, userDetail);
    }

    @Transactional(readOnly = true)
    public List<UserDetailsDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> {
                    UserDetail userDetail = userDetailRepository.findByUserId(user.getId())
                            .orElseThrow(() -> new RuntimeException("User detail not found for user id: " + user.getId()));
                    return mapToUserDetailsDto(user, userDetail);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDetailsDto> getSubordinates(Long managerId) {
        return userRepository.findAllByManagerId(managerId).stream()
                .map(user -> {
                    UserDetail userDetail = userDetailRepository.findByUserId(user.getId())
                            .orElseThrow(() -> new RuntimeException("User detail not found for user id: " + user.getId()));
                    return mapToUserDetailsDto(user, userDetail);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDetailsDto updateUserDetails(Long userId, UserDetailsDto userDetailsDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        UserDetail userDetail = userDetailRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User detail not found for user id: " + userId));
        
        // Update user fields if provided
        if (userDetailsDto.getEmail() != null && !userDetailsDto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userDetailsDto.getEmail())) {
                throw new RuntimeException("Email is already taken");
            }
            user.setEmail(userDetailsDto.getEmail());
        }
        
        if (userDetailsDto.getRole() != null) {
            user.setRole(userDetailsDto.getRole());
        }
        
     // Update availability if provided
        if (userDetailsDto.isAvailable() != user.isAvailable()) {
            user.setAvailable(userDetailsDto.isAvailable());
        }
        
        if (userDetailsDto.getManagerId() != null && !userDetailsDto.getManagerId().equals(user.getManager() != null ? user.getManager().getId() : null)) {
            User manager = userRepository.findById(userDetailsDto.getManagerId())
                    .orElseThrow(() -> new RuntimeException("Manager not found with id: " + userDetailsDto.getManagerId()));
            user.setManager(manager);
        }
        
        // Update user detail fields if provided
        if (userDetailsDto.getFirstName() != null) {
            userDetail.setFirstName(userDetailsDto.getFirstName());
        }
        
        if (userDetailsDto.getLastName() != null) {
            userDetail.setLastName(userDetailsDto.getLastName());
        }
        
        if (userDetailsDto.getDepartment() != null) {
            userDetail.setDepartment(userDetailsDto.getDepartment());
        }
        
        if (userDetailsDto.getPosition() != null) {
            userDetail.setPosition(userDetailsDto.getPosition());
        }
        
        if (userDetailsDto.getPhoneNumber() != null) {
            userDetail.setPhoneNumber(userDetailsDto.getPhoneNumber());
        }
        
        if (userDetailsDto.getAnnualLeaveAllowance() > 0) {
            userDetail.setAnnualLeaveAllowance(userDetailsDto.getAnnualLeaveAllowance());
        }
        
        userRepository.save(user);
        userDetailRepository.save(userDetail);
        
        return mapToUserDetailsDto(user, userDetail);
    }
    
    @Transactional
    public UserDetailsDto updateUserAvailability(Long userId, boolean available) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        user.setAvailable(available);
        User savedUser = userRepository.save(user);
        
        UserDetail userDetail = userDetailRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User detail not found for user id: " + userId));
        
        return mapToUserDetailsDto(savedUser, userDetail);
    }


    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    @Transactional
    public void updateLeaveAllowance(Long userId, int daysUsed) {
        UserDetail userDetail = userDetailRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User detail not found for user id: " + userId));
        
        int remainingDays = userDetail.getRemainingLeaveAllowance() - daysUsed;
        if (remainingDays < 0) {
            throw new RuntimeException("Not enough leave days available");
        }
        
        userDetail.setRemainingLeaveAllowance(remainingDays);
        userDetailRepository.save(userDetail);
    }
    
    @Transactional(readOnly = true)
    public User findAvailableApprover(User employee) {
        if (employee == null || employee.getManager() == null) {
            return null;
        }
        
        User manager = employee.getManager();
        
        // If the direct manager is available, return them
        if (manager.isAvailable()) {
            return manager;
        }
        
        // Otherwise try to find a superior manager by traversing up the chain
        while (manager.getManager() != null) {
            manager = manager.getManager();
            if (manager.isAvailable()) {
                return manager;
            }
        }
        
     // If no available manager found in the chain, return ANY admin regardless of availability
        List<User> admins = userRepository.findByRole("ADMIN");
        if (!admins.isEmpty()) {
            // Return the first admin without checking availability
            return admins.get(0);
        }
        
        // No available approver found
        return null;
    }

    private UserDetailsDto mapToUserDetailsDto(User user, UserDetail userDetail) {
        UserDetailsDto dto = new UserDetailsDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(userDetail.getFirstName());
        dto.setLastName(userDetail.getLastName());
        dto.setDepartment(userDetail.getDepartment());
        dto.setPosition(userDetail.getPosition());
        dto.setPhoneNumber(userDetail.getPhoneNumber());
        dto.setRole(user.getRole());
        dto.setAvailable(user.isAvailable());
        
        if (user.getManager() != null) {
            dto.setManagerId(user.getManager().getId());
            UserDetail managerDetail = userDetailRepository.findByUserId(user.getManager().getId())
                    .orElse(null);
            if (managerDetail != null) {
                dto.setManagerName(managerDetail.getFirstName() + " " + managerDetail.getLastName());
            }
        }
        
        dto.setAnnualLeaveAllowance(userDetail.getAnnualLeaveAllowance());
        dto.setRemainingLeaveAllowance(userDetail.getRemainingLeaveAllowance());
        
        return dto;
    }
}