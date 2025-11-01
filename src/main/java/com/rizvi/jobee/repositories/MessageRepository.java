package com.rizvi.jobee.repositories;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rizvi.jobee.entities.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("select m from Message m where m.receiverId = :userId and m.receiverType = 'USER' or m.senderId = :userId and m.senderType = 'USER'")
    List<Message> findMessagesForUserId(Long userId);

    @Query("select m from Message m where m.receiverId = :userId and m.receiverType = 'BUSINESS' or m.senderId = :userId and m.senderType = 'BUSINESS'")
    List<Message> findMessagesForBusinessId(Long businessId);

    @Query("select m from Message m where m.conversation.id = :conversationId")
    List<Message> findMessagesByConversationId(Long conversationId, Sort sort);

    @Query("select m from Message m where m.conversation.id = :conversationId and m.read = false")
    List<Message> findUnreadMessagesInConversationForUser(Long conversationId, Long userId, String userType);
}
