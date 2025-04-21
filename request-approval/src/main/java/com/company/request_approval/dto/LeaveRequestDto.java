package com.company.request_approval.dto;

import jakarta.validation.constraints.NotBlank;  
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class LeaveRequestDto {
    
    private Long id;
    
    private Long userId;
    
    private String userName;
    
    private Long currentApproverId;
    private String currentApproverName;

    private Long originalManagerId;
    private String originalManagerName;
    
    @NotNull
    private LocalDate startDate;
    
    @NotNull
    private LocalDate endDate;
    
    private int durationDays;
    
    @NotBlank
    private String leaveType;
    
    private String reason;
    
    private String status;
    
    private String rejectionReason;
    
    private String department;
    
    private String position;
    private String escalationHistory;

	private Object escalationStatus;
    
    public LeaveRequestDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public Long getCurrentApproverId() {
        return currentApproverId;
    }

    public void setCurrentApproverId(Long currentApproverId) {
        this.currentApproverId = currentApproverId;
    }

    public String getCurrentApproverName() {
        return currentApproverName;
    }

    public void setCurrentApproverName(String currentApproverName) {
        this.currentApproverName = currentApproverName;
    }
    
	public Long getOriginalManagerId() {
		return originalManagerId;
	}

	public void setOriginalManagerId(Long originalManagerId) {
		this.originalManagerId = originalManagerId;
	}

	public String getOriginalManagerName() {
		return originalManagerName;
	}

	public void setOriginalManagerName(String originalManagerName) {
		this.originalManagerName = originalManagerName;
	}

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(int durationDays) {
        this.durationDays = durationDays;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
    
    public String getEscalationHistory() {
        return escalationHistory;
    }

    public void setEscalationHistory(String escalationHistory) {
        this.escalationHistory = escalationHistory;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

	public void setEscalationStatus(Object escalationStatus) {
		this.escalationStatus=escalationStatus;
		
	}

}
