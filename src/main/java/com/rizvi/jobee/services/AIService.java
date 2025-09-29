package com.rizvi.jobee.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.audio.transcriptions.Transcription;
import com.openai.models.audio.transcriptions.TranscriptionCreateParams;
import com.openai.models.audio.transcriptions.TranscriptionCreateResponse;
import com.openai.models.audio.speech.SpeechCreateParams;
import com.openai.models.audio.speech.SpeechModel;
import com.openai.models.audio.speech.SpeechCreateParams.ResponseFormat;
import com.openai.models.audio.speech.SpeechCreateParams.Voice;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.StructuredChatCompletionCreateParams;
import com.openai.models.realtime.AudioTranscription;
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
        byte[] audioData = response.body().readAllBytes();
        return audioData;
    }

    public String speechToText(MultipartFile audioFile) throws IOException {
        // Until OpenAI SDK supports MultipartFile directly, we need to convert it to
        // a temp file which then will be cleaned up. The fools better get it together
        System.out.println(audioFile.getOriginalFilename());
        System.out.println(audioFile.getContentType());
        String originalFileName = audioFile.getOriginalFilename();
        Path tempFile = Files.createTempFile("open-audio-", originalFileName);
        try (InputStream in = audioFile.getInputStream()) {
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error copying file to temp location: " + e.getMessage());
            // Throw an error
            return null;
        }

        try {
            TranscriptionCreateParams params = TranscriptionCreateParams.builder()
                    .model("whisper-1")
                    .file(tempFile)
                    .build();
            TranscriptionCreateResponse transcription = openAIClient.audio().transcriptions().create(params);
            String response = transcription.asTranscription().text();
            // Clean up and remove the temp file
            Files.deleteIfExists(tempFile);
            return response;
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("Error during transcription: " + e.getMessage());
        }
        return null;
    }
}
