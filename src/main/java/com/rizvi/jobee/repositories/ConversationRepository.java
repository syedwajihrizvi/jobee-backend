package com.rizvi.jobee.repositories;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rizvi.jobee.entities.Conversation;
import com.rizvi.jobee.enums.MessagerUserType;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
        @Query("select c from Conversation c where " +
                        "(c.participantOneId = :userId and c.participantOneType = :userType) or " +
                        "(c.participantTwoId = :userId and c.participantTwoType = :userType)")
        List<Conversation> findConversationsForUserIdAndUserType(Long userId, MessagerUserType userType, Sort sort);

        @Query("select c from Conversation c where " +
                        "((c.participantOneId = :userId and c.participantOneType = :userType) or " +
                        "(c.participantTwoId = :userId and c.participantTwoType = :userType)) and " +
                        "(lower(c.participantOneName) like lower(concat('%', :search, '%')) or " +
                        " lower(c.participantTwoName) like lower(concat('%', :search, '%')) )")
        List<Conversation> findConversationsForUserIdAndUserTypeWithSearch(Long userId, MessagerUserType userType,
                        Sort sort,
                        String search);

        @Query("select c from Conversation c where " +
                        "(c.participantOneId = :idOne and c.participantOneType = :typeOne and " +
                        " c.participantTwoId = :idTwo and c.participantTwoType = :typeTwo) or " +
                        "(c.participantOneId = :idTwo and c.participantOneType = :typeTwo and " +
                        " c.participantTwoId = :idOne and c.participantTwoType = :typeOne)")
        Conversation findConversationBetweenParticipants(Long idOne, MessagerUserType typeOne, Long idTwo,
                        MessagerUserType typeTwo);
}
