package com.company.request_approval.repository;

import com.company.request_approval.model.User; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    
    Boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.manager.id = :managerId")
    List<User> findAllByManagerId(Long managerId);
    List<User> findByRole(String role);
}
