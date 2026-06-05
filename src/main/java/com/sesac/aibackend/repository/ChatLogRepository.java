package com.sesac.aibackend.repository;

import com.sesac.aibackend.domain.ChatLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatLogRepository extends JpaRepository<ChatLog, Long> {


    List<ChatLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("""
          select c from ChatLog c
          join fetch c.user
          where c.user.id = :userId
          order by c.createdAt desc
          """)
    List<ChatLog> findByUserIdWithUser(Long userId);
}
