package com.example.project_chat.repository;

import com.example.project_chat.entity.ConversationMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationMemberRepository extends JpaRepository<ConversationMember, Integer> {
}
