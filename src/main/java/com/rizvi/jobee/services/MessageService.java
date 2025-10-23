package com.rizvi.jobee.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.rizvi.jobee.dtos.message.ConversationDto;
import com.rizvi.jobee.entities.Conversation;
import com.rizvi.jobee.entities.Message;
import com.rizvi.jobee.enums.MessagerUserType;
import com.rizvi.jobee.enums.Role;
import com.rizvi.jobee.repositories.BusinessAccountRepository;
import com.rizvi.jobee.repositories.ConversationRepository;
import com.rizvi.jobee.repositories.MessageRepository;
import com.rizvi.jobee.repositories.UserProfileRepository;

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
                return profileInfos;
            }
        }
        return null;
    }

    public List<ConversationDto> getConversationsForUser(Long userId, String userRole) {
        var userType = userRole.equals(Role.BUSINESS.name()) || userRole.equals(Role.ADMIN.name())
                ? MessagerUserType.BUSINESS
                : MessagerUserType.USER;
        System.out.println("Determined userType: " + userType);
        Sort sort = Sort.by("updatedAt").descending();
        List<Conversation> conversations = conversationRepository.findConversationsForUserIdAndUserType(userId,
                userType, sort);
        List<ConversationDto> conversationDtos = new ArrayList<>();
        for (Conversation conversation : conversations) {
            // Get the last message details
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
                // Set the participant info accoringly
            } else {
                conversationDto.setWasLastMessageSender(false);
                var profileInfo = getMessageProfileImageUrlAndName(lastMessage.getSenderId(),
                        lastMessage.getSenderType());
                conversationDto.setParticipantName(profileInfo.get("name"));
                conversationDto.setParticipantProfileImageUrl(profileInfo.get("profileImageUrl"));
                conversationDto.setParticipantId(Long.valueOf(profileInfo.get("id")));
                // Set the participant info accordingly
            }
            conversationDtos.add(conversationDto);
        }
        return conversationDtos;
    }
}
