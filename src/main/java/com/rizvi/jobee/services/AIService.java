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
import com.openai.models.audio.transcriptions.TranscriptionCreateParams;
import com.openai.models.audio.transcriptions.TranscriptionCreateResponse;
import com.openai.models.audio.speech.SpeechCreateParams;
import com.openai.models.audio.speech.SpeechModel;
import com.openai.models.audio.speech.SpeechCreateParams.ResponseFormat;
import com.openai.models.audio.speech.SpeechCreateParams.Voice;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.StructuredChatCompletionCreateParams;
import com.rizvi.jobee.dtos.user.ResumeExtract;
import com.rizvi.jobee.entities.UserProfile;
import com.rizvi.jobee.exceptions.InvalidDocumentException;
import com.rizvi.jobee.helpers.Prompts;
import com.rizvi.jobee.helpers.AISchemas.AICandidate;
import com.rizvi.jobee.helpers.AISchemas.AIJobEnhancementAnswer;
import com.rizvi.jobee.helpers.AISchemas.AIJobInsightAnswer;
import com.rizvi.jobee.helpers.AISchemas.AIJobTagsResponse;
import com.rizvi.jobee.helpers.AISchemas.AIProfessionalSummaryAnswer;
import com.rizvi.jobee.helpers.AISchemas.AnswerInterviewQuestionRequest;
import com.rizvi.jobee.helpers.AISchemas.AnswerInterviewQuestionResponse;
import com.rizvi.jobee.helpers.AISchemas.GenerateAIInsightRequest;
import com.rizvi.jobee.helpers.AISchemas.GenerateAIJobDescriptionRequest;
import com.rizvi.jobee.helpers.AISchemas.GenerateAIProfessionalSummaryRequest;
import com.rizvi.jobee.helpers.AISchemas.PostedJobInformationRequest;
import com.rizvi.jobee.helpers.AISchemas.PrepareForInterviewRequest;
import com.rizvi.jobee.helpers.AISchemas.PrepareForInterviewResponse;
import com.rizvi.jobee.helpers.AISchemas.ReferenceToPreviousAnswer;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class AIService {
    private final OpenAIClient openAIClient;

    public ResumeExtract extractDetailsFromResume(MultipartFile resumeFile, UserProfile userProfile)
            throws IOException {
        String resumeText = ResumeExtractService.extractText(resumeFile);
        AICandidate candidate = new AICandidate(userProfile);
        // TODO: Validate the file is actually a resume. Use AI to check if it is a
        String resumeTextLower = resumeText.toLowerCase();
        if (!(resumeTextLower.contains("experience") || resumeTextLower.contains("skills")
                || resumeTextLower.contains("education"))) {
            throw new InvalidDocumentException("The uploaded file does not appear to be a valid resume.");
        }
        String prompt = Prompts.RESUME_ANALYSIS.replace("{resumeText}", resumeText)
                .replace("{existingCandidateInfoJSON}", candidate.toJsonString());
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
                .model(SpeechModel.TTS_1_HD)
                .input(text)
                .voice(Voice.ECHO).responseFormat(ResponseFormat.MP3).speed(1.0f).build();
        var response = openAIClient.audio().speech().create(params);
        byte[] audioData = response.body().readAllBytes();
        return audioData;
    }

    public String speechToText(MultipartFile audioFile) throws IOException {
        // Until OpenAI SDK supports MultipartFile directly, we need to convert it to
        // a temp file which then will be cleaned up. The fools better get it together
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
            Files.deleteIfExists(tempFile);
            return response;
        } catch (Exception e) {
            // TODO: handle exception
            System.out.println("Error during transcription: " + e.getMessage());
        }
        return null;
    }

    public AnswerInterviewQuestionResponse answerInterviewQuestion(
            AnswerInterviewQuestionRequest request) throws IOException {
        String inputJson = request.toJsonString();
        String prompt = Prompts.INTERVIEW_PREP_QUESTION_ANSWER.replace("{inputJSON}", inputJson);
        try {
            StructuredChatCompletionCreateParams<AnswerInterviewQuestionResponse> params = ChatCompletionCreateParams
                    .builder()
                    .model(ChatModel.GPT_5)
                    .addSystemMessage("You are a helpful assistant that helps candidates answer interview questions.")
                    .addUserMessage(prompt)
                    .responseFormat(AnswerInterviewQuestionResponse.class)
                    .build();
            Optional<AnswerInterviewQuestionResponse> result = openAIClient.chat().completions().create(params)
                    .choices()
                    .stream()
                    .flatMap(choice -> choice.message().content().stream()).findFirst();
            return result.orElse(null);
        } catch (Exception e) {
            System.out.println("Error during answering interview question: " + e.getMessage());
            return null;
        }
    }

    public AnswerInterviewQuestionResponse getFeedbackForAnswer(
            AnswerInterviewQuestionRequest request, ReferenceToPreviousAnswer previousAnswer) {
        String inputJson = request.toJsonString();
        String referenceJson = previousAnswer.toJsonString();
        String prompt = Prompts.INTERVIEW_PREP_QUESTION_ANSWER_FEEDBACK.replace("{inputJSON}", inputJson)
                .replace("{referenceJSON}", referenceJson);
        try {
            StructuredChatCompletionCreateParams<AnswerInterviewQuestionResponse> params = ChatCompletionCreateParams
                    .builder()
                    .model(ChatModel.GPT_5)
                    .addSystemMessage("You are a helpful assistant that helps candidates answer interview questions.")
                    .addUserMessage(prompt)
                    .responseFormat(AnswerInterviewQuestionResponse.class)
                    .build();
            Optional<AnswerInterviewQuestionResponse> result = openAIClient.chat().completions().create(params)
                    .choices()
                    .stream()
                    .flatMap(choice -> choice.message().content().stream()).findFirst();
            return result.orElse(null);
        } catch (Exception e) {
            System.out.println("Error during answering interview question: " + e.getMessage());
            return null;
        }
    }

    public AIJobInsightAnswer generateAIJobInsight(GenerateAIInsightRequest request) {
        System.out.println("Generating AI Job Insight for job");
        System.out.println(request.toJsonString());
        String inputJson = request.toJsonString();
        String prompt = Prompts.JOB_INSIGHT_GENERATION.replace("{inputJSON}", inputJson);

        try {
            StructuredChatCompletionCreateParams<AIJobInsightAnswer> params = ChatCompletionCreateParams.builder()
                    .model(ChatModel.GPT_5_NANO)
                    .addSystemMessage("You are a helpful assistant that provides insights about job postings.")
                    .addUserMessage(prompt)
                    .responseFormat(AIJobInsightAnswer.class)
                    .build();
            System.out.println("Calling OpenAI for job insights...");
            Optional<AIJobInsightAnswer> result = openAIClient.chat().completions().create(params).choices().stream()
                    .flatMap(choice -> choice.message().content().stream()).findFirst();
            System.out.println("Received job insights from OpenAI.");
            return result.orElse(null);
        } catch (Exception e) {
            System.out.println("Error during generating job insights: " + e.getMessage());
            return null;
        }
    }

    public AIJobEnhancementAnswer enhanceJobCreation(GenerateAIJobDescriptionRequest request) {
        String inputJson = request.toJsonString();
        String prompt = Prompts.JOB_CREATION_ENHANCE.replace("{inputJSON}", inputJson);
        System.out.println(inputJson);
        try {
            StructuredChatCompletionCreateParams<AIJobEnhancementAnswer> params = ChatCompletionCreateParams.builder()
                    .model(ChatModel.GPT_5_NANO)
                    .addSystemMessage("You are a helpful assistant that generates job descriptions.")
                    .addUserMessage(prompt)
                    .responseFormat(AIJobEnhancementAnswer.class)
                    .build();
            Optional<AIJobEnhancementAnswer> result = openAIClient.chat().completions().create(params).choices()
                    .stream()
                    .flatMap(choice -> choice.message().content().stream()).findFirst();
            return result.orElse(null);
        } catch (Exception e) {
            System.out.println("Error during generating job description: " + e.getMessage());
            return null;
        }
    }

    public AIProfessionalSummaryAnswer generateAIProfessionalSummary(GenerateAIProfessionalSummaryRequest request) {
        String inputJson = request.toJsonString();
        String prompt = Prompts.PROFESSIONAL_SUMMARY_GENERATION.replace("{inputJSON}", inputJson);
        try {
            StructuredChatCompletionCreateParams<AIProfessionalSummaryAnswer> params = ChatCompletionCreateParams
                    .builder().model(ChatModel.GPT_5_NANO)
                    .addSystemMessage("You are a helpful assistant that generates professional summaries for users.")
                    .addUserMessage(prompt)
                    .responseFormat(AIProfessionalSummaryAnswer.class)
                    .build();
            Optional<AIProfessionalSummaryAnswer> result = openAIClient.chat().completions().create(params).choices()
                    .stream().flatMap(choice -> choice.message().content().stream()).findFirst();
            return result.orElse(null);
        } catch (Exception e) {
            System.out.println("Error during generating professional summary: " + e.getMessage());
            return null;
        }
    }
}
