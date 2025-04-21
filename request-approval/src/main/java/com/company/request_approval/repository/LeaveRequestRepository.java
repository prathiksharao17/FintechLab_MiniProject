package com.company.request_approval.repository;

import com.company.request_approval.model.LeaveRequest; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByUserId(Long userId);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.user.manager.id = :managerId AND lr.status = 'PENDING'")
    List<LeaveRequest> findPendingRequestsByManagerId(@Param("managerId") Long managerId);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.user.id = :userId AND lr.status = :status")
    List<LeaveRequest> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.startDate <= :endDate AND lr.endDate >= :startDate AND lr.user.id = :userId AND lr.status = 'APPROVED'")
    List<LeaveRequest> findOverlappingLeaveRequests(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.startDate <= :endDate AND lr.endDate >= :startDate AND lr.user.id = :userId AND lr.status = 'APPROVED' AND lr.id != :leaveRequestId")
    List<LeaveRequest> findOverlappingLeaveRequestsExcludingCurrent(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("leaveRequestId") Long leaveRequestId);
    
    int countByUserIdAndStatus(Long userId, String status);
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.user.id = :userId AND lr.startDate >= :currentDate AND lr.status = 'APPROVED' ORDER BY lr.startDate ASC")
    List<LeaveRequest> findUpcomingLeaveRequests(@Param("userId") Long userId, @Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.currentApprover.id = :approverId AND lr.status = 'PENDING'")
    List<LeaveRequest> findPendingRequestsByApproverId(@Param("approverId") Long approverId);

    /**
     * Find escalated requests - where the current approver is not the employee's direct manager
     * @param approverId ID of the manager who is currently assigned to approve the request
     * @return List of leave requests that have been escalated to this manager
     */
    @Query("SELECT lr FROM LeaveRequest lr " +
           "WHERE lr.currentApprover.id = :approverId " +
           "AND lr.status = 'PENDING' " +
           "AND lr.user.manager.id != :approverId")
    List<LeaveRequest> findEscalatedRequestsForApprover(@Param("approverId") Long approverId);
    
    @Query("SELECT lr FROM LeaveRequest lr " +
            "WHERE lr.currentApprover.id = :managerId " +
            "AND lr.status = 'PENDING' " +
            "AND lr.user.manager.id = :managerId")
     List<LeaveRequest> findDirectReportPendingRequestsForManager(@Param("managerId") Long managerId);
}