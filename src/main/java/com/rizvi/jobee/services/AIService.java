package com.rizvi.jobee.services;

import java.io.IOException;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.audio.speech.SpeechCreateParams;
import com.openai.models.audio.speech.SpeechModel;
import com.openai.models.audio.speech.SpeechCreateParams.ResponseFormat;
import com.openai.models.audio.speech.SpeechCreateParams.Voice;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.StructuredChatCompletionCreateParams;
import com.rizvi.jobee.dtos.user.ResumeExtract;
import com.rizvi.jobee.exceptions.InvalidDocumentException;
import com.rizvi.jobee.helpers.Prompts;
import com.rizvi.jobee.helpers.AISchemas.PrepareForInterviewRequest;
import com.rizvi.jobee.helpers.AISchemas.PrepareForInterviewResponse;

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
            return result.get();
        }
        return null;
    }

    public PrepareForInterviewResponse generateInterviewPrep(PrepareForInterviewRequest prepareForInterview)
            throws IOException {
        // Convert prepareForInterview to JSON
        String inputJson = prepareForInterview.toJsonString();
        String prompt = Prompts.INTERVIEW_PREP.replace("{inputJSON}", inputJson);

        StructuredChatCompletionCreateParams<PrepareForInterviewResponse> params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_5)
                .addSystemMessage("You are a helpful assistant that prepares candidates for interviews.")
                .addUserMessage(prompt)
                .responseFormat(PrepareForInterviewResponse.class)
                .build();
        Optional<PrepareForInterviewResponse> result = openAIClient.chat().completions().create(params).choices()
                .stream()
                .flatMap(choice -> choice.message().content().stream()).findFirst();
        return result.orElse(null);
    }

    public byte[] textToSpeech(String text) throws IOException {
        SpeechCreateParams params = SpeechCreateParams.builder()
                .model(SpeechModel.GPT_4O_MINI_TTS)
                .input(text)
                .voice(Voice.SAGE).responseFormat(ResponseFormat.MP3).speed(1.0f).build();
        var response = openAIClient.audio().speech().create(params);
        System.out.println("Received audio response with status code: " + response.statusCode());
        byte[] audioData = response.body().readAllBytes();
        System.out.println("Generated audio data of length: " + audioData.length);
        return audioData;
    }
}
