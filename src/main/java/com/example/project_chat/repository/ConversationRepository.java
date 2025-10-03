package com.example.project_chat.repository;

import com.example.project_chat.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Integer> {
    @Query("SELECT cm1.conversationId " +
            "FROM ConversationMember cm1 " +
            "JOIN ConversationMember cm2 ON cm1.conversationId = cm2.conversationId " +
            "JOIN Conversation c ON cm1.conversationId = c.id " +
            "WHERE c.type = 'DIRECT' " +
            "AND cm1.userId = :user1Id " +
            "AND cm2.userId = :user2Id")
    Optional<Integer> findDirectConversationIdByUserIds(@Param("user1Id") Integer user1Id, @Param("user2Id") Integer user2Id);
}
