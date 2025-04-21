package com.company.request_approval.service;

import com.company.request_approval.dto.LeaveRequestDto; 
import com.company.request_approval.model.LeaveRequest;
import com.company.request_approval.model.User;
import com.company.request_approval.model.UserDetail;
import com.company.request_approval.repository.LeaveRequestRepository;
import com.company.request_approval.repository.UserDetailRepository;
import com.company.request_approval.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;
    private final UserDetailRepository userDetailRepository;
    private final UserService userService;

    public LeaveRequestService(LeaveRequestRepository leaveRequestRepository,
                               UserRepository userRepository,
                               UserDetailRepository userDetailRepository,
                               UserService userService) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.userRepository = userRepository;
        this.userDetailRepository = userDetailRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestDto> getAllLeaveRequests() {
        return leaveRequestRepository.findAll().stream()
                .map(this::mapToLeaveRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestDto> getLeaveRequestsByUserId(Long userId) {
        return leaveRequestRepository.findByUserId(userId).stream()
                .map(this::mapToLeaveRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestDto> getLeaveRequestsByUserIdAndStatus(Long userId, String status) {
        return leaveRequestRepository.findByUserIdAndStatus(userId, status).stream()
                .map(this::mapToLeaveRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LeaveRequestDto> getPendingRequestsByManagerId(Long managerId) {
        return leaveRequestRepository.findPendingRequestsByManagerId(managerId).stream()
                .map(this::mapToLeaveRequestDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<LeaveRequestDto> getPendingRequestsByApproverId(Long approverId) {
        return leaveRequestRepository.findPendingRequestsByApproverId(approverId).stream()
                .map(this::mapToLeaveRequestDto)
                .collect(Collectors.toList());
    }
    
    /**
     * Get leave requests that have been escalated to this manager
     * (requests where they are the approver but not the employee's direct manager)
     * 
     * @param approverId ID of the manager who is currently assigned to approve the request
     * @return List of leave request DTOs that have been escalated to this manager
     */
    @Transactional(readOnly = true)
    public List<LeaveRequestDto> getEscalatedRequestsForApprover(Long approverId) {
        return leaveRequestRepository.findEscalatedRequestsForApprover(approverId).stream()
                .map(request -> {
                    LeaveRequestDto dto = mapToLeaveRequestDto(request);
                    
                    // Add the original manager information for UI display if available
                    User originalManager = request.getUser().getManager();
                    if (originalManager != null) {
                        dto.setOriginalManagerId(originalManager.getId());
                        
                        UserDetail managerDetail = userDetailRepository.findByUserId(originalManager.getId())
                                .orElse(null);
                        
                        if (managerDetail != null) {
                            dto.setOriginalManagerName(managerDetail.getFirstName() + " " + managerDetail.getLastName());
                        } else {
                            dto.setOriginalManagerName(originalManager.getEmail());
                        }
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    @Transactional
    public LeaveRequestDto updateEscalatedRequestStatus(Long id, String status, String rejectionReason, Long managerId) {
        // Fetch the leave request by its ID
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Escalated leave request not found with id: " + id));
        
        // Check if the manager has the correct permission to update this request
        // For example, checking if the current escalated request's manager is the same as the one updating
        if (!leaveRequest.getCurrentApprover().getId().equals(managerId)) {
            throw new RuntimeException("You are not authorized to update the status of this escalated request");
        }

        // Store the previous escalation status to compare if changes are needed
        String previousEscalationStatus = leaveRequest.getEscalationStatus();
        
        // Update the status of the escalation request
        leaveRequest.setEscalationStatus(status);
        
        // If the request is being rejected, set the rejection reason
        if ("REJECTED".equals(status)) {
            leaveRequest.setRejectionReason(rejectionReason);
        }

        // Additional logic for specific status transitions can be added here
        // Example: if the request is approved, handle any necessary actions (e.g., updating user records)
        if ("APPROVED".equals(status) && !"APPROVED".equals(previousEscalationStatus)) {
            // Implement logic for approved requests (e.g., notify user, update leave allowance, etc.)
            // Assuming there is logic related to the approval process
        }
        
        // Handle any additional transitions based on status
        if (("REJECTED".equals(status) || "CANCELED".equals(status)) && "APPROVED".equals(previousEscalationStatus)) {
            // Logic to handle cases where a request was approved and is now being rejected or canceled
            // For example, refunding the leave days or other business-specific logic
        }

        // Save the updated leave request status
        LeaveRequest updatedLeaveRequest = leaveRequestRepository.save(leaveRequest);
        
        // Return the updated LeaveRequestDto
        return mapToLeaveRequestDto(updatedLeaveRequest);
    }
    
    @Transactional(readOnly = true)
    public List<LeaveRequestDto> getDirectReportPendingRequests(Long managerId) {
        return leaveRequestRepository.findDirectReportPendingRequestsForManager(managerId).stream()
                .map(this::mapToLeaveRequestDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LeaveRequestDto getLeaveRequestById(Long id) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found with id: " + id));
        return mapToLeaveRequestDto(leaveRequest);
    }

    @Transactional
    public LeaveRequestDto createLeaveRequest(LeaveRequestDto leaveRequestDto) {
        User user = userRepository.findById(leaveRequestDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + leaveRequestDto.getUserId()));
        
        // Calculate duration in days
        int durationDays = calculateDurationInDays(leaveRequestDto.getStartDate(), leaveRequestDto.getEndDate());
        
        // Check for overlapping leave requests
        List<LeaveRequest> overlappingRequests = leaveRequestRepository.findOverlappingLeaveRequests(
                user.getId(), leaveRequestDto.getStartDate(), leaveRequestDto.getEndDate());
        
        if (!overlappingRequests.isEmpty()) {
            throw new RuntimeException("You already have an approved leave request for this period");
        }
        
        // Check if user has enough leave days left
        if ("ANNUAL".equals(leaveRequestDto.getLeaveType())) {
            UserDetail userDetail = userDetailRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("User detail not found for user id: " + user.getId()));
            
            if (userDetail.getRemainingLeaveAllowance() < durationDays) {
                throw new RuntimeException("Not enough leave days available. You have " + 
                        userDetail.getRemainingLeaveAllowance() + " days left, but requested " + durationDays + " days");
            }
        }
        
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setUser(user);
        leaveRequest.setStartDate(leaveRequestDto.getStartDate());
        leaveRequest.setEndDate(leaveRequestDto.getEndDate());
        leaveRequest.setDurationDays(durationDays);
        leaveRequest.setLeaveType(leaveRequestDto.getLeaveType());
        leaveRequest.setReason(leaveRequestDto.getReason());
        leaveRequest.setStatus("PENDING");
        
        // Find the available approver (either direct manager or escalated)
        User approver = userService.findAvailableApprover(user);
        if (approver == null) {
            throw new RuntimeException("No available approver found in the management chain or among admins");
        }
        
        leaveRequest.setCurrentApprover(approver);
        
        // If the approver is not the direct manager, add an escalation entry
        if (user.getManager() != null && !approver.getId().equals(user.getManager().getId())) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String escalationEntry = LocalDateTime.now().format(formatter) + ": Request escalated from " 
                    + getUserFullName(user.getManager()) + " to " + getUserFullName(approver) 
                    + " due to manager unavailability";
            leaveRequest.addEscalationEntry(escalationEntry);
        }
        
        LeaveRequest savedLeaveRequest = leaveRequestRepository.save(leaveRequest);
        return mapToLeaveRequestDto(savedLeaveRequest);
    }

    @Transactional
    public LeaveRequestDto updateLeaveRequest(LeaveRequestDto leaveRequestDto) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestDto.getId())
                .orElseThrow(() -> new RuntimeException("Leave request not found with id: " + leaveRequestDto.getId()));
        
        // Check if the request can be updated (only PENDING requests can be updated)
        if (!"PENDING".equals(leaveRequest.getStatus())) {
            throw new RuntimeException("Only pending requests can be updated");
        }
        
        // Calculate new duration
        int durationDays = calculateDurationInDays(leaveRequestDto.getStartDate(), leaveRequestDto.getEndDate());
        
        // Check for overlapping requests (excluding the current one)
        List<LeaveRequest> overlappingRequests = leaveRequestRepository.findOverlappingLeaveRequestsExcludingCurrent(
                leaveRequestDto.getUserId(), leaveRequestDto.getStartDate(), leaveRequestDto.getEndDate(), leaveRequestDto.getId());
        
        if (!overlappingRequests.isEmpty()) {
            throw new RuntimeException("You already have an approved leave request for this period");
        }
        
        // Check if user has enough leave days left if it's an annual leave
        if ("ANNUAL".equals(leaveRequestDto.getLeaveType())) {
            UserDetail userDetail = userDetailRepository.findByUserId(leaveRequestDto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User detail not found for user id: " + leaveRequestDto.getUserId()));
            
            if (userDetail.getRemainingLeaveAllowance() < durationDays) {
                throw new RuntimeException("Not enough leave days available. You have " + 
                        userDetail.getRemainingLeaveAllowance() + " days left, but requested " + durationDays + " days");
            }
        }
        
        // Update fields
        leaveRequest.setStartDate(leaveRequestDto.getStartDate());
        leaveRequest.setEndDate(leaveRequestDto.getEndDate());
        leaveRequest.setDurationDays(durationDays);
        leaveRequest.setLeaveType(leaveRequestDto.getLeaveType());
        leaveRequest.setReason(leaveRequestDto.getReason());
        
        LeaveRequest updatedLeaveRequest = leaveRequestRepository.save(leaveRequest);
        return mapToLeaveRequestDto(updatedLeaveRequest);
    }

    @Transactional
    public LeaveRequestDto updateLeaveRequestStatus(Long id, String status, String rejectionReason, Long managerId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Leave request not found with id: " + id));
        
        // Verify manager has permission to update this request
        // This would depend on your data model - implement as needed
        
        if (!leaveRequest.getCurrentApprover().getId().equals(managerId)) {
            throw new RuntimeException("You are not authorized to approve/reject this request");
        }
        
        String previousStatus = leaveRequest.getStatus();
        leaveRequest.setStatus(status);
        
        if ("REJECTED".equals(status)) {
            leaveRequest.setRejectionReason(rejectionReason);
        }
        
        // If request is being approved and is for annual leave, update the remaining leave allowance
        if ("APPROVED".equals(status) && !"APPROVED".equals(previousStatus) && "ANNUAL".equals(leaveRequest.getLeaveType())) {
            userService.updateLeaveAllowance(leaveRequest.getUser().getId(), leaveRequest.getDurationDays());
        }
        
        // If request was approved but is now being rejected or canceled, refund the leave days
        if (("REJECTED".equals(status) || "CANCELED".equals(status)) && "APPROVED".equals(previousStatus) && "ANNUAL".equals(leaveRequest.getLeaveType())) {
            UserDetail userDetail = userDetailRepository.findByUserId(leaveRequest.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("User detail not found for user id: " + leaveRequest.getUser().getId()));
            
            userDetail.setRemainingLeaveAllowance(userDetail.getRemainingLeaveAllowance() + leaveRequest.getDurationDays());
            userDetailRepository.save(userDetail);
        }
        
        LeaveRequest updatedLeaveRequest = leaveRequestRepository.save(leaveRequest);
        return mapToLeaveRequestDto(updatedLeaveRequest);
    }

    @Transactional
    public boolean deleteLeaveRequest(Long id) {
        try {
            LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Leave request not found with id: " + id));
            
            // If request was approved and is being deleted, refund the leave days
            if ("APPROVED".equals(leaveRequest.getStatus()) && "ANNUAL".equals(leaveRequest.getLeaveType())) {
                UserDetail userDetail = userDetailRepository.findByUserId(leaveRequest.getUser().getId())
                        .orElseThrow(() -> new RuntimeException("User detail not found for user id: " + leaveRequest.getUser().getId()));
                
                userDetail.setRemainingLeaveAllowance(userDetail.getRemainingLeaveAllowance() + leaveRequest.getDurationDays());
                userDetailRepository.save(userDetail);
            }
            
            leaveRequestRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getLeaveRequestSummaryForUser(Long userId) {
        Map<String, Object> summary = new HashMap<>();
        
        // Get user details to check leave allowance
        UserDetail userDetail = userDetailRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User detail not found for user id: " + userId));
        
        // Count approved, pending, and rejected requests
        int approvedCount = leaveRequestRepository.countByUserIdAndStatus(userId, "APPROVED");
        int pendingCount = leaveRequestRepository.countByUserIdAndStatus(userId, "PENDING");
        int rejectedCount = leaveRequestRepository.countByUserIdAndStatus(userId, "REJECTED");
        
        // Get upcoming leave requests
        List<LeaveRequestDto> upcomingLeaves = leaveRequestRepository.findUpcomingLeaveRequests(userId, LocalDate.now())
                .stream()
                .map(this::mapToLeaveRequestDto)
                .collect(Collectors.toList());
        
        summary.put("remainingLeaveAllowance", userDetail.getRemainingLeaveAllowance());
        summary.put("approvedRequestsCount", approvedCount);
        summary.put("pendingRequestsCount", pendingCount);
        summary.put("rejectedRequestsCount", rejectedCount);
        summary.put("upcomingLeaves", upcomingLeaves);
        
        return summary;
    }
    
    @Transactional
    public Map<String, Integer> getLeaveStatistics(Long userId) {
        Map<String, Integer> statistics = new HashMap<>();
        
        int totalRequests = leaveRequestRepository.findByUserId(userId).size();
        int pendingRequests = leaveRequestRepository.countByUserIdAndStatus(userId, "PENDING");
        int approvedRequests = leaveRequestRepository.countByUserIdAndStatus(userId, "APPROVED");
        int rejectedRequests = leaveRequestRepository.countByUserIdAndStatus(userId, "REJECTED");
        
        statistics.put("totalRequests", totalRequests);
        statistics.put("pendingRequests", pendingRequests);
        statistics.put("approvedRequests", approvedRequests);
        statistics.put("rejectedRequests", rejectedRequests);
        
        return statistics;
    }
    
    @Transactional
    public LeaveRequestDto handleManagerUnavailability(Long managerId) {
        // Mark the manager as unavailable
        userService.updateUserAvailability(managerId, false);
        
        // Get all pending requests for this manager
        List<LeaveRequest> pendingRequests = leaveRequestRepository.findPendingRequestsByManagerId(managerId);
        if (pendingRequests.isEmpty()) {
            return null;
        }
        
        // Process the first request as an example
        LeaveRequest request = pendingRequests.get(0);
        User requester = request.getUser();
        
        // Find the next available approver in the management chain
        User newApprover = userService.findAvailableApprover(requester);
        if (newApprover == null) {
            throw new RuntimeException("No available approver found in the management chain or among admins");
        }
        
        // Update the request with the new approver
        User oldApprover = request.getCurrentApprover();
        request.setCurrentApprover(newApprover);
        
        // Add an escalation history entry
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String escalationEntry = LocalDateTime.now().format(formatter) + ": Request escalated from " 
                + getUserFullName(oldApprover) + " to " + getUserFullName(newApprover) 
                + " due to manager unavailability";
        request.addEscalationEntry(escalationEntry);
        
        LeaveRequest updatedRequest = leaveRequestRepository.save(request);
        return mapToLeaveRequestDto(updatedRequest);
    }
    
    @Transactional
    public List<LeaveRequestDto> reassignAllPendingRequests(Long managerId) {
        // Get all pending requests for this manager
        List<LeaveRequest> pendingRequests = leaveRequestRepository.findPendingRequestsByManagerId(managerId);
        
        return pendingRequests.stream().map(request -> {
            User requester = request.getUser();
            
            // Find the next available approver in the management chain
            User newApprover = userService.findAvailableApprover(requester);
            if (newApprover == null) {
                throw new RuntimeException("No available approver found for user " + requester.getId());
            }
            
            // Update the request with the new approver
            User oldApprover = request.getCurrentApprover();
            request.setCurrentApprover(newApprover);
            
            // Add an escalation history entry
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String escalationEntry = LocalDateTime.now().format(formatter) + ": Request reassigned from " 
                    + getUserFullName(oldApprover) + " to " + getUserFullName(newApprover) 
                    + " by administrator";
            request.addEscalationEntry(escalationEntry);
            
            LeaveRequest updatedRequest = leaveRequestRepository.save(request);
            return mapToLeaveRequestDto(updatedRequest);
        }).collect(Collectors.toList());
    }

    private int calculateDurationInDays(LocalDate startDate, LocalDate endDate) {
        return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }
    
    private String getUserFullName(User user) {
        if (user == null) {
            return "Unknown";
        }
        
        UserDetail userDetail = userDetailRepository.findByUserId(user.getId()).orElse(null);
        if (userDetail == null) {
            return user.getEmail();
        }
        
        return userDetail.getFirstName() + " " + userDetail.getLastName();
    }


    private LeaveRequestDto mapToLeaveRequestDto(LeaveRequest leaveRequest) {
        LeaveRequestDto dto = new LeaveRequestDto();
        dto.setId(leaveRequest.getId());
        dto.setUserId(leaveRequest.getUser().getId());
        
        UserDetail userDetail = userDetailRepository.findByUserId(leaveRequest.getUser().getId())
                .orElse(null);
        
        if (userDetail != null) {
            dto.setUserName(userDetail.getFirstName() + " " + userDetail.getLastName());
            dto.setDepartment(userDetail.getDepartment());
            dto.setPosition(userDetail.getPosition());
        }
        
     // Set current approver information
        if (leaveRequest.getCurrentApprover() != null) {
            dto.setCurrentApproverId(leaveRequest.getCurrentApprover().getId());
            
            UserDetail approverDetail = userDetailRepository.findByUserId(leaveRequest.getCurrentApprover().getId())
                    .orElse(null);
            
            if (approverDetail != null) {
                dto.setCurrentApproverName(approverDetail.getFirstName() + " " + approverDetail.getLastName());
            }
        }
        
        dto.setStartDate(leaveRequest.getStartDate());
        dto.setEndDate(leaveRequest.getEndDate());
        dto.setDurationDays(leaveRequest.getDurationDays());
        dto.setLeaveType(leaveRequest.getLeaveType());
        dto.setReason(leaveRequest.getReason());
        dto.setStatus(leaveRequest.getStatus());
        dto.setRejectionReason(leaveRequest.getRejectionReason());
        dto.setEscalationHistory(leaveRequest.getEscalationHistory());
        
        return dto;
    }
}