package com.example.project_chat.repository;

import com.example.project_chat.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {
    Set<UserRole> findByUserId(Integer userId);
}
