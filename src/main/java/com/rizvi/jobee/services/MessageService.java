package com.rizvi.jobee.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.rizvi.jobee.dtos.message.ConversationDto;
import com.rizvi.jobee.dtos.message.ConversationMessageRequestDto;
import com.rizvi.jobee.entities.Conversation;
import com.rizvi.jobee.entities.Message;
import com.rizvi.jobee.enums.MessagerUserType;
import com.rizvi.jobee.enums.Role;
import com.rizvi.jobee.repositories.BusinessAccountRepository;
import com.rizvi.jobee.repositories.ConversationRepository;
import com.rizvi.jobee.repositories.MessageRepository;
import com.rizvi.jobee.repositories.UserProfileRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final BusinessAccountRepository businessAccountRepository;
    private final UserProfileRepository userProfileRepository;

    public List<Message> getMessagesForUser(Long userId, String userRole) {
        List<Message> messages;
        if (userRole.equals(Role.BUSINESS.name()) || userRole.equals(Role.ADMIN.name())) {
            messages = messageRepository.findMessagesForBusinessId(userId);
        } else {
            messages = messageRepository.findMessagesForUserId(userId);
        }
        return messages;
    }

    public Map<String, String> getMessageProfileImageUrlAndName(Long userId, MessagerUserType userType) {
        var profileInfos = new HashMap<String, String>();
        if (userType == MessagerUserType.BUSINESS) {
            var businessAccount = businessAccountRepository.findById(userId).orElse(null);
            if (businessAccount != null) {
                var name = businessAccount.getFullName();
                var profileImageUrl = businessAccount.getProfile().getProfileImageUrl();
                profileInfos.put("name", name);
                profileInfos.put("profileImageUrl", profileImageUrl);
                profileInfos.put("id", businessAccount.getId().toString());
                profileInfos.put("role", MessagerUserType.BUSINESS.name());
                return profileInfos;
            }
        } else {
            var userProfile = userProfileRepository.findById(userId).orElse(null);
            if (userProfile != null) {
                var name = userProfile.getFullName();
                var profileImageUrl = userProfile.getProfileImageUrl();
                profileInfos.put("name", name);
                profileInfos.put("profileImageUrl", profileImageUrl);
                profileInfos.put("id", userProfile.getId().toString());
                profileInfos.put("role", MessagerUserType.USER.name());
                return profileInfos;
            }
        }
        return null;
    }

    public List<ConversationDto> getConversationsForUser(Long userId, String userRole, String search) {
        var userType = userRole.equals(Role.BUSINESS.name()) || userRole.equals(Role.ADMIN.name())
                ? MessagerUserType.BUSINESS
                : MessagerUserType.USER;
        System.out.println("Search Param Received in Service: " + search);
        Sort sort = Sort.by("updatedAt").descending();
        List<Conversation> conversations = null;
        if (search != null && !search.isEmpty()) {
            conversations = conversationRepository.findConversationsForUserIdAndUserTypeWithSearch(userId, userType,
                    sort, search);
        } else {
            conversations = conversationRepository.findConversationsForUserIdAndUserType(userId,
                    userType, sort);
        }
        List<ConversationDto> conversationDtos = new ArrayList<>();
        for (Conversation conversation : conversations) {

            Long lastMessageId = conversation.getLastMessageId();
            Message lastMessage = messageRepository.findById(lastMessageId).orElse(null);
            var conversationDto = new ConversationDto();
            conversationDto.setId(conversation.getId());
            conversationDto.setLastMessageContent(lastMessage.getText());
            conversationDto.setLastMessageRead(lastMessage.getRead());
            conversationDto.setLastMessageTimestamp(lastMessage.getTimestamp());
            if (lastMessage.getSenderId().equals(userId) && lastMessage.getSenderType() == userType) {
                conversationDto.setWasLastMessageSender(true);
                var profileInfo = getMessageProfileImageUrlAndName(lastMessage.getReceiverId(),
                        lastMessage.getReceiverType());
                conversationDto.setParticipantName(profileInfo.get("name"));
                conversationDto.setParticipantProfileImageUrl(profileInfo.get("profileImageUrl"));
                conversationDto.setParticipantId(Long.valueOf(profileInfo.get("id")));
                conversationDto.setParticipantRole(profileInfo.get("role"));
            } else {
                conversationDto.setWasLastMessageSender(false);
                var profileInfo = getMessageProfileImageUrlAndName(lastMessage.getSenderId(),
                        lastMessage.getSenderType());
                conversationDto.setParticipantName(profileInfo.get("name"));
                conversationDto.setParticipantProfileImageUrl(profileInfo.get("profileImageUrl"));
                conversationDto.setParticipantId(Long.valueOf(profileInfo.get("id")));
                conversationDto.setParticipantRole(profileInfo.get("role"));
                // Set the participant info accordingly
            }
            conversationDtos.add(conversationDto);
        }
        return conversationDtos;
    }

    public List<Message> getMessagesBetweenUsers(Long userId, String userRole,
            ConversationMessageRequestDto requestDto) {
        Long conversationId = requestDto.getConversationId();
        Sort sort = Sort.by("timestamp").ascending();
        List<Message> messages = messageRepository.findMessagesByConversationId(conversationId, sort);
        return messages;
    }

    public Conversation getConversationBetweenUsers(Long userId, String userRole,
            Long otherPartyId, String otherPartyRole) {
        var userType = userRole.equals("BUSINESS") || userRole.equals("ADMIN")
                ? MessagerUserType.BUSINESS
                : MessagerUserType.USER;
        var otherPartyType = otherPartyRole.equals("BUSINESS") || otherPartyRole.equals("ADMIN")
                ? MessagerUserType.BUSINESS
                : MessagerUserType.USER;
        return conversationRepository.findConversationBetweenParticipants(
                userId, userType, otherPartyId, otherPartyType);
    }

    public Conversation createConversationBetweenUsers(
            Long userId, String userRole,
            Long otherPartyId, String otherPartyRole) {
        var userType = userRole.equals("BUSINESS") || userRole.equals("ADMIN")
                ? MessagerUserType.BUSINESS
                : MessagerUserType.USER;
        var otherPartyType = otherPartyRole.equals("BUSINESS") || otherPartyRole.equals("ADMIN")
                ? MessagerUserType.BUSINESS
                : MessagerUserType.USER;
        // Check if conversation already exists
        Conversation existingConversation = conversationRepository.findConversationBetweenParticipants(
                userId, userType, otherPartyId, otherPartyType);
        if (existingConversation != null) {
            return existingConversation;
        }
        // Get the names of the participants
        String participantOneName = null;
        String participantTwoName = null;
        if (userType == MessagerUserType.BUSINESS) {
            var businessAccount = businessAccountRepository.findById(userId).orElse(null);
            if (businessAccount != null) {
                participantOneName = businessAccount.getFullName();
            }
        } else {
            var userProfile = userProfileRepository.findById(userId).orElse(null);
            if (userProfile != null) {
                participantOneName = userProfile.getFullName();
            }
        }
        if (otherPartyType == MessagerUserType.BUSINESS) {
            var businessAccount = businessAccountRepository.findById(otherPartyId).orElse(null);
            if (businessAccount != null) {
                participantTwoName = businessAccount.getFullName();
            }
        } else {
            var userProfile = userProfileRepository.findById(otherPartyId).orElse(null);
            if (userProfile != null) {
                participantTwoName = userProfile.getFullName();
            }
        }
        // Create new conversation
        Conversation conversation = new Conversation();
        conversation.setParticipantOneId(userId);
        conversation.setParticipantOneType(userType);
        conversation.setParticipantTwoId(otherPartyId);
        conversation.setParticipantTwoType(otherPartyType);
        conversation.setParticipantOneName(participantOneName);
        conversation.setParticipantTwoName(participantTwoName);
        return conversationRepository.save(conversation);
    }

    @Transactional
    public Message saveMessage(Message message) {
        // Check to see if a conversation exists between the two parties
        Long senderId = message.getSenderId();
        Long receiverId = message.getReceiverId();
        MessagerUserType senderType = message.getSenderType();
        MessagerUserType receiverType = message.getReceiverType();
        Conversation conversation = conversationRepository.findConversationBetweenParticipants(
                senderId, senderType, receiverId, receiverType);
        // Create new conversation if it does not exist
        if (conversation == null) {
            conversation = new Conversation();
            conversation.setParticipantOneId(senderId);
            conversation.setParticipantOneType(senderType);
            conversation.setParticipantTwoId(receiverId);
            conversation.setParticipantTwoType(receiverType);
            conversation = conversationRepository.save(conversation);
        }
        // Add the message to the conversation
        message.setConversation(conversation);
        Message savedMessage = messageRepository.save(message);
        conversation.setLastMessageId(savedMessage.getId());
        conversationRepository.save(conversation);
        return savedMessage;
    }
}
