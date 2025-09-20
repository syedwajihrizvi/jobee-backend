package com.rizvi.jobee.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.rizvi.jobee.entities.UserDocument;
import com.rizvi.jobee.entities.UserProfile;
import com.rizvi.jobee.enums.UserDocumentType;
import com.rizvi.jobee.exceptions.InvalidDocumentException;
import com.rizvi.jobee.repositories.UserDocumentRepository;
import com.rizvi.jobee.repositories.UserProfileRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class UserDocumentService {
    private final S3Service s3Service;
    private final UserDocumentRepository userDocumentRepository;
    private final UserProfileRepository userProfileRepository;
    private final AIService aiService;
    private final EducationService userEducationService;
    private final ExperienceService userExperienceService;
    private final UserSkillService userSkillService;

    public String uploadDocument(
            Long userId, MultipartFile document, UserDocumentType documentType) {
        try {
            var result = s3Service.uploadDocument(userId, document, documentType);
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public List<UserDocument> getUserDocuments(Long userId) {
        return userDocumentRepository.findByUserId(userId);
    }

    public UserDocument createUserDocumentViaFile(
            MultipartFile document, UserDocumentType documentType, UserProfile userProfile,
            String title, Boolean setPrimary) {
        if (document.getSize() > 200_000) {
            throw new InvalidDocumentException("File size exceeds the maximum limit of 200KB");
        }
        var userId = userProfile.getId();
        var result = uploadDocument(userId, document, documentType);
        if (result == null) {
            return null;
        }
        var userDocument = UserDocument.builder().documentType(documentType)
                .documentUrl(result).title(title).user(userProfile).build();
        // If document is resume type, then we can update user profile information using
        // AIService
        if (documentType == UserDocumentType.RESUME) {
            extractResumeDetailsAndPopulateProfile(document, userProfile);
        }
        userProfile.addDocument(userDocument);
        if (setPrimary) {
            userProfile.setPrimaryResume(userDocument);
        }
        userProfileRepository.save(userProfile);
        return userDocument;
    }

    public boolean extractResumeDetailsAndPopulateProfile(MultipartFile resume, UserProfile userProfile) {
        try {
            var details = aiService.extractDetailsFromResume(resume);
            var educations = details.getEducation();
            var experiences = details.getExperience();
            var skills = details.getSkills();
            System.out.println("SYED-DEBUG: Extracted educations: " + educations);
            System.out.println("SYED-DEBUG: Extracted experiences: " + experiences);
            System.out.println("SYED-DEBUG: Extracted skills: " + skills);
            userEducationService.createEducationsForUserFromAISchemas(educations, userProfile);
            System.out.println("SYED-DEBUG: Added educations");
            userSkillService.createUserSkills(skills, userProfile);
            System.out.println("SYED-DEBUG: Added skills");
            userExperienceService.addExperiencesForUserFromAISchemas(experiences, userProfile);
            System.out.println("SYED-DEBUG: Added experiences");
            return true;
        } catch (Exception e) {
            // Log the error but continue
            System.err.println("Failed to extract details from resume: " + e.getMessage());
            return false;
        }
    }
}
