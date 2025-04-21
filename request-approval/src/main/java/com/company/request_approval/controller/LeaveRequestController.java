package com.company.request_approval.controller;

import com.company.request_approval.dto.LeaveRequestDto;
import com.company.request_approval.dto.UserDetailsDto;
import com.company.request_approval.security.JwtTokenProvider;
import com.company.request_approval.service.LeaveRequestService;
import com.company.request_approval.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/leave-requests")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public LeaveRequestController(LeaveRequestService leaveRequestService, 
                                  UserService userService,
                                  JwtTokenProvider jwtTokenProvider) {
        this.leaveRequestService = leaveRequestService;
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LeaveRequestDto>> getAllLeaveRequests() {
        List<LeaveRequestDto> leaveRequests = leaveRequestService.getAllLeaveRequests();
        return ResponseEntity.ok(leaveRequests);
    }

    @GetMapping("/profile")
    public ResponseEntity<List<LeaveRequestDto>> getMyLeaveRequests(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        String token = authHeader.substring(7);
        
        String email = jwtTokenProvider.getUsernameFromJWT(token);
        UserDetailsDto user = userService.getUserDetailsByEmail(email);
        List<LeaveRequestDto> leaveRequests = leaveRequestService.getLeaveRequestsByUserId(user.getId());
        return ResponseEntity.ok(leaveRequests);
    }

    @GetMapping("/me/status/{status}")
    public ResponseEntity<List<LeaveRequestDto>> getMyLeaveRequestsByStatus(
            HttpServletRequest request,
            @PathVariable String status) {
        
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        String token = authHeader.substring(7);
        
        String email = jwtTokenProvider.getUsernameFromJWT(token);
        UserDetailsDto user = userService.getUserDetailsByEmail(email);
        List<LeaveRequestDto> leaveRequests = leaveRequestService.getLeaveRequestsByUserIdAndStatus(user.getId(), status);
        return ResponseEntity.ok(leaveRequests);
    }
    
    @GetMapping("/pending-approval")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<LeaveRequestDto>> getPendingApprovalRequests(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        String token = authHeader.substring(7);
        
        String email = jwtTokenProvider.getUsernameFromJWT(token);
        UserDetailsDto manager = userService.getUserDetailsByEmail(email);
        List<LeaveRequestDto> pendingRequests = leaveRequestService.getDirectReportPendingRequests(manager.getId());
        return ResponseEntity.ok(pendingRequests);
    }
    
    @GetMapping("/escalated-requests")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<LeaveRequestDto>> getEscalatedRequests(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        String token = authHeader.substring(7);
        
        String email = jwtTokenProvider.getUsernameFromJWT(token);
        UserDetailsDto manager = userService.getUserDetailsByEmail(email);
        List<LeaveRequestDto> escalatedRequests = leaveRequestService.getEscalatedRequestsForApprover(manager.getId());
        return ResponseEntity.ok(escalatedRequests);
    }
    

    @GetMapping("/{id}")
    public ResponseEntity<LeaveRequestDto> getLeaveRequestById(@PathVariable Long id) {
        LeaveRequestDto leaveRequest = leaveRequestService.getLeaveRequestById(id);
        return ResponseEntity.ok(leaveRequest);
    }

    @PostMapping
    public ResponseEntity<LeaveRequestDto> createLeaveRequest(
            HttpServletRequest request,
            @Valid @RequestBody LeaveRequestDto leaveRequestDto) {
        
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        String token = authHeader.substring(7);
        
        String email = jwtTokenProvider.getUsernameFromJWT(token);
        UserDetailsDto user = userService.getUserDetailsByEmail(email);
        
        leaveRequestDto.setUserId(user.getId());
        LeaveRequestDto createdRequest = leaveRequestService.createLeaveRequest(leaveRequestDto);
        return ResponseEntity.ok(createdRequest);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<LeaveRequestDto> updateLeaveRequestStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate,
            HttpServletRequest request) {
        
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        String token = authHeader.substring(7);
        
        String email = jwtTokenProvider.getUsernameFromJWT(token);
        UserDetailsDto manager = userService.getUserDetailsByEmail(email);
        
        String newStatus = statusUpdate.get("status");
        String rejectionReason = statusUpdate.get("comments");
        
        LeaveRequestDto updatedRequest = leaveRequestService.updateLeaveRequestStatus(id, newStatus, rejectionReason, manager.getId());
        return ResponseEntity.ok(updatedRequest);
    }
    
    @PutMapping("/escalated-requests/{id}/status")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<LeaveRequestDto> updateEscalatedRequestStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate,
            HttpServletRequest request) {
        
        // Extract token from the Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build(); // Unauthorized if token is missing or invalid
        }
        String token = authHeader.substring(7);
        
        // Extract username from the JWT token
        String email = jwtTokenProvider.getUsernameFromJWT(token);
        UserDetailsDto manager = userService.getUserDetailsByEmail(email);
        
        // Get the new status and rejection reason from the request body
        String newStatus = statusUpdate.get("status");
        String rejectionReason = statusUpdate.get("comments");
        
        // Call the service layer to update the escalation status
        LeaveRequestDto updatedRequest = leaveRequestService.updateEscalatedRequestStatus(id, newStatus, rejectionReason, manager.getId());
        
        // Return the updated leave request DTO in the response
        return ResponseEntity.ok(updatedRequest);
    }
    
    
    @PutMapping("/{id}")
    public ResponseEntity<LeaveRequestDto> updateLeaveRequest(
            @PathVariable Long id,
            @Valid @RequestBody LeaveRequestDto leaveRequestDto,
            HttpServletRequest request) {
        
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        String token = authHeader.substring(7);
        
        String email = jwtTokenProvider.getUsernameFromJWT(token);
        UserDetailsDto user = userService.getUserDetailsByEmail(email);
        
        // Ensure the user can only update their own requests
        LeaveRequestDto existingRequest = leaveRequestService.getLeaveRequestById(id);
        if (!existingRequest.getUserId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        leaveRequestDto.setId(id);
        leaveRequestDto.setUserId(user.getId());
        LeaveRequestDto updatedRequest = leaveRequestService.updateLeaveRequest(leaveRequestDto);
        return ResponseEntity.ok(updatedRequest);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteLeaveRequest(
            @PathVariable Long id,
            HttpServletRequest request) {
        
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        String token = authHeader.substring(7);
        
        String email = jwtTokenProvider.getUsernameFromJWT(token);
        UserDetailsDto user = userService.getUserDetailsByEmail(email);
        
        // Ensure the user can only delete their own requests or has admin role
        LeaveRequestDto existingRequest = leaveRequestService.getLeaveRequestById(id);
        if (!existingRequest.getUserId().equals(user.getId()) && 
                !request.isUserInRole("ROLE_ADMIN")) {
            return ResponseEntity.status(403).build();
        }
        
        boolean deleted = leaveRequestService.deleteLeaveRequest(id);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", deleted);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getLeaveRequestSummary(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        String token = authHeader.substring(7);
        
        String email = jwtTokenProvider.getUsernameFromJWT(token);
        UserDetailsDto user = userService.getUserDetailsByEmail(email);
        
        Map<String, Object> summary = leaveRequestService.getLeaveRequestSummaryForUser(user.getId());
        return ResponseEntity.ok(summary);
    }
}