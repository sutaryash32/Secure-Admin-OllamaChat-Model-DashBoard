package com.ai.demo.repository;

import com.ai.demo.model.ChatHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
    
    List<ChatHistory> findByUserEmailOrderByCreatedAtDesc(String userEmail);
    
    Page<ChatHistory> findByUserEmail(String userEmail, Pageable pageable);
    
    Long countByUserEmail(String userEmail);
    
    Long countByUserEmailAndChatType(String userEmail, ChatHistory.ChatType chatType);
}
