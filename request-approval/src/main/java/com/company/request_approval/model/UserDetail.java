package com.company.request_approval.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_details")
public class UserDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column
    private String department;

    @Column
    private String position;

    @Column
    private String phoneNumber;

    @Column
    private int annualLeaveAllowance;

    @Column
    private int remainingLeaveAllowance;

    public UserDetail() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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