package com.company.request_approval.dto;

public class UserDetailsDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String department;
    private String position;
    private String phoneNumber;
    private String role;
    private Long managerId;
    private String managerName;
    private boolean available;
    private int annualLeaveAllowance;
    private int remainingLeaveAllowance;

    public UserDetailsDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }
    
    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public int getAnnualLeaveAllowance() {
        return annualLeaveAllowance;
    }

    public void setAnnualLeaveAllowance(int annualLeaveAllowance) {
        this.annualLeaveAllowance = annualLeaveAllowance;
    }

    public int getRemainingLeaveAllowance() {
        return remainingLeaveAllowance;
    }

    public void setRemainingLeaveAllowance(int remainingLeaveAllowance) {
        this.remainingLeaveAllowance = remainingLeaveAllowance;
    }
}