package com.rizvi.jobee.services;

import java.io.IOException;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.StructuredChatCompletionCreateParams;
import com.rizvi.jobee.dtos.user.ResumeExtract;
import com.rizvi.jobee.exceptions.InvalidDocumentException;
import com.rizvi.jobee.helpers.Prompts;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class AIService {
    private final OpenAIClient openAIClient;

    public ResumeExtract extractDetailsFromResume(MultipartFile resumeFile) throws IOException {
        String resumeText = ResumeExtractService.extractText(resumeFile);
        // TODO: Validate the file is actually a resume. Use AI to check if it is a
        // resume
        // For now just check if experience, skillls, education words are present
        String resumeTextLower = resumeText.toLowerCase();
        if (!(resumeTextLower.contains("experience") || resumeTextLower.contains("skills")
                || resumeTextLower.contains("education"))) {
            System.out.println("SYED-DEBUG: Invalid resume content");
            throw new InvalidDocumentException("The uploaded file does not appear to be a valid resume.");
        }
        String prompt = Prompts.RESUME_ANALYSIS.replace("{resumeText}", resumeText);

        StructuredChatCompletionCreateParams<ResumeExtract> params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_5_NANO)
                .addSystemMessage("You are a helpful assistant that extracts structured information from resumes.")
                .addUserMessage(prompt)
                .responseFormat(ResumeExtract.class)
                .build();

        Optional<ResumeExtract> result = openAIClient.chat().completions().create(params).choices().stream()
                .flatMap(choice -> choice.message().content().stream()).findFirst();

        if (result.isPresent()) {
            System.out.println("SYED-DEBUG: Extracted resume details: " + result.get());
            return result.get();
        }
        return null;
    }
}
