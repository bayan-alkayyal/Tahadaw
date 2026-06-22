package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.AI.AiJsonParser;
import org.example.tahadaw.AI.AiService;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.DTO.IN.AiGeneratedQuestionDTOIn;
import org.example.tahadaw.DTO.IN.AiQuestionAnswerItemDTOIn;
import org.example.tahadaw.DTO.IN.AiQuestionAnswersSubmitDTOIn;
import org.example.tahadaw.DTO.OUT.AiGeneratedQuestionDTOOut;
import org.example.tahadaw.DTO.OUT.AiQuestionAnswerDTOOut;
import org.example.tahadaw.Model.AiGeneratedQuestion;
import org.example.tahadaw.Model.AiQuestionAnswer;
import org.example.tahadaw.Model.GiftPlan;
import org.example.tahadaw.Model.Recipient;
import org.example.tahadaw.Model.RequiredQuestionAnswer;
import org.example.tahadaw.Repository.AiGeneratedQuestionRepository;
import org.example.tahadaw.Repository.AiQuestionAnswerRepository;
import org.example.tahadaw.Repository.GiftPlanRepository;
import org.example.tahadaw.Repository.RequiredQuestionAnswerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AiQuestionService {

    private final AiGeneratedQuestionRepository aiGeneratedQuestionRepository;
    private final AiQuestionAnswerRepository aiQuestionAnswerRepository;
    private final GiftPlanRepository giftPlanRepository;
    private final RequiredQuestionAnswerRepository requiredQuestionAnswerRepository;
    private final AiService aiService;

    // shahad-CRUD

    public void createAiQuestion(Long gifPlanId, AiGeneratedQuestionDTOIn request) {
        GiftPlan giftPlan = giftPlanRepository.findGiftPlanById(gifPlanId).orElse(null);
        if (giftPlan == null) {
            throw new IllegalArgumentException("Gift plan not found");
        }

        AiGeneratedQuestion aiGeneratedQuestion = new AiGeneratedQuestion();
        aiGeneratedQuestion.setQuestionText(request.getQuestionText());
        aiGeneratedQuestion.setReasonForQuestion(request.getReasonForQuestion());
        aiGeneratedQuestion.setGiftPlan(giftPlan);

        aiGeneratedQuestionRepository.save(aiGeneratedQuestion);
    }

    public List<AiGeneratedQuestion> getAllAiQuestion() {
        return aiGeneratedQuestionRepository.findAll();
    }

    public void updateAiQuestion(long id, AiGeneratedQuestionDTOIn request) {
        AiGeneratedQuestion oldAiGeneratedQuestion = getAiQuestionById(id);
        oldAiGeneratedQuestion.setQuestionText(request.getQuestionText());
        oldAiGeneratedQuestion.setReasonForQuestion(request.getReasonForQuestion());
        aiGeneratedQuestionRepository.save(oldAiGeneratedQuestion);
    }

    public void deleteAiQuestion(Long id) {
        AiGeneratedQuestion aiGeneratedQuestion = getAiQuestionById(id);
        aiGeneratedQuestionRepository.delete(aiGeneratedQuestion);
    }

    public AiGeneratedQuestion getAiQuestionById(Long id) {
        AiGeneratedQuestion aiGeneratedQuestion = aiGeneratedQuestionRepository.findAiGeneratedQuestionById(id).orElse(null);
        if (aiGeneratedQuestion == null) {
            throw new IllegalArgumentException("Ai question not found");
        }
        return aiGeneratedQuestion;
    }

    // shahad-gift-plan flow

    @Transactional
    public List<AiGeneratedQuestion> generateQuestions(Long userId, Long giftPlanId) {
        GiftPlan giftPlan = requireOwnedGiftPlan(userId, giftPlanId);

         if (!"REQUIRED_QUESTIONS_ANSWERED".equals(giftPlan.getStatus())) {
             throw new ApiException("Answer all required questions before generating AI questions.");
         }

        if (aiGeneratedQuestionRepository.existsByGiftPlan_Id(giftPlanId)) {
            throw new ApiException("AI questions already generated for this gift plan.");
        }

        String prompt = buildPrompt(giftPlan);
        //get the questions from the ai service as json node
        JsonNode root = AiJsonParser.parseObject(aiService.ask(prompt));
        JsonNode questionsNode = root.get("questions");

        //check if the questions node is null or empty
        if (questionsNode == null || !questionsNode.isArray() || questionsNode.isEmpty()) {
            throw new ApiException("AI did not return any questions.");
        }

        LocalDateTime now = LocalDateTime.now();

        List<AiGeneratedQuestion> aiGeneratedQuestion = new ArrayList<>();
        int order = 0;
        for (JsonNode questionNode : questionsNode) {
            AiGeneratedQuestion question = new AiGeneratedQuestion();
            question.setGiftPlan(giftPlan);
            question.setQuestionText(AiJsonParser.requireText(questionNode, "questionText"));
            question.setReasonForQuestion(AiJsonParser.requireText(questionNode, "reasonForQuestion"));
            question.setCreatedAt(now);
            question.setDisplayOrder(order++);

            aiGeneratedQuestion.add(question);
            aiGeneratedQuestionRepository.save(question);
        }

        giftPlan.setStatus("AI_QUESTIONS_GENERATED");
        giftPlan.setUpdatedAt(now);
        giftPlanRepository.save(giftPlan);

        return aiGeneratedQuestion;
    }

    /**
     * Throws away the current AI questions (and any answers to them) and asks the AI
     * for a fresh set. Unlike {@link #generateQuestions} this does not block when
     * questions already exist; it replaces them and resets the plan back to
     * AI_QUESTIONS_GENERATED so the user answers the new batch.
     */
    @Transactional
    public List<AiGeneratedQuestionDTOOut> regenerateQuestions(Long userId, Long giftPlanId) {
        GiftPlan giftPlan = requireOwnedGiftPlan(userId, giftPlanId);

        List<AiGeneratedQuestion> existing =
                aiGeneratedQuestionRepository.findByGiftPlan_IdOrderByDisplayOrderAsc(giftPlanId);

        // remember what was already asked so we can tell the AI not to repeat it
        List<String> previousQuestions = new ArrayList<>();
        for (AiGeneratedQuestion question : existing) {
            if (question.getQuestionText() != null && !question.getQuestionText().isBlank()) {
                previousQuestions.add(question.getQuestionText());
            }
        }

        if (!existing.isEmpty()) {
            List<AiQuestionAnswer> oldAnswers =
                    aiQuestionAnswerRepository.findByAiGeneratedQuestion_GiftPlan_IdOrderByCreatedAtAsc(giftPlanId);
            if (!oldAnswers.isEmpty()) {
                aiQuestionAnswerRepository.deleteAll(oldAnswers);
            }
            aiGeneratedQuestionRepository.deleteAll(existing);
            aiGeneratedQuestionRepository.flush();
        }

        String prompt = buildRegeneratePrompt(giftPlan, previousQuestions);
        JsonNode root = AiJsonParser.parseObject(aiService.ask(prompt));
        JsonNode questionsNode = root.get("questions");

        if (questionsNode == null || !questionsNode.isArray() || questionsNode.isEmpty()) {
            throw new ApiException("AI did not return any questions.");
        }

        LocalDateTime now = LocalDateTime.now();
        List<AiGeneratedQuestionDTOOut> regenerated = new ArrayList<>();
        int order = 0;
        for (JsonNode questionNode : questionsNode) {
            AiGeneratedQuestion question = new AiGeneratedQuestion();
            question.setGiftPlan(giftPlan);
            question.setQuestionText(AiJsonParser.requireText(questionNode, "questionText"));
            question.setReasonForQuestion(AiJsonParser.requireText(questionNode, "reasonForQuestion"));
            question.setCreatedAt(now);
            question.setDisplayOrder(order++);
            regenerated.add(toQuestionDto(aiGeneratedQuestionRepository.save(question)));
        }

        giftPlan.setStatus("AI_QUESTIONS_GENERATED");
        giftPlan.setUpdatedAt(now);
        giftPlanRepository.save(giftPlan);

        return regenerated;
    }

    /**
     * Same context prompt as {@link #buildPrompt}, plus an explicit instruction not to
     * repeat any of the previously generated questions.
     */
    private String buildRegeneratePrompt(GiftPlan giftPlan, List<String> previousQuestions) {
        String basePrompt = buildPrompt(giftPlan);

        if (previousQuestions == null || previousQuestions.isEmpty()) {
            return basePrompt;
        }

        StringBuilder exclude = new StringBuilder();
        exclude.append("\nDo not repeat any of these previously asked questions:\n");
        for (String previous : previousQuestions) {
            exclude.append("- ").append(previous).append('\n');
        }
        exclude.append("Ask different questions this time.\n");

        return basePrompt + exclude;
    }

    public List<AiGeneratedQuestionDTOOut> listQuestions(Long userId, Long giftPlanId) {
        requireOwnedGiftPlan(userId, giftPlanId);

        List<AiGeneratedQuestion> questions = aiGeneratedQuestionRepository
                .findByGiftPlan_IdOrderByDisplayOrderAsc(giftPlanId);

        List<AiGeneratedQuestionDTOOut> result = new ArrayList<>();
        for (AiGeneratedQuestion question : questions) {
            result.add(toQuestionDto(question));
        }
        return result;
    }



    private GiftPlan requireOwnedGiftPlan(Long userId, Long giftPlanId) {
        GiftPlan giftPlan = giftPlanRepository.findGiftPlanById(giftPlanId)
                .orElseThrow(() -> new ApiException("Gift plan not found."));
        if (!giftPlan.getUser().getId().equals(userId)) {
            throw new ApiException("Gift plan not found.");
        }
        if (!giftPlan.getRecipient().getUser().getId().equals(userId)) {
            throw new ApiException("Recipient must belong to the gift plan owner.");
        }
        return giftPlan;
    }

    //Shahad
    public String buildPrompt(GiftPlan giftPlan) {
        //bring the recipient and required answers
        Recipient recipient = giftPlan.getRecipient();
        List<RequiredQuestionAnswer> requiredAnswers =
                requiredQuestionAnswerRepository.findRequiredQuestionAnswerByGiftPlan(giftPlan);

        StringBuilder context = new StringBuilder();
        context.append("Recipient name: ").append(recipient.getName()).append('\n');
        if (recipient.getRelationship() != null) {
            context.append("Relationship: ").append(recipient.getRelationship()).append('\n');
        }
        if (recipient.getAge() != null) {
            context.append("Age: ").append(recipient.getAge()).append('\n');
        }
        if (recipient.getGender() != null) {
            context.append("Gender: ").append(recipient.getGender()).append('\n');
        }
        if (recipient.getInterests() != null) {
            context.append("Interests: ").append(recipient.getInterests()).append('\n');
        }
        if (recipient.getHobbies() != null) {
            context.append("Hobbies: ").append(recipient.getHobbies()).append('\n');
        }
        if (recipient.getDislikes() != null) {
            context.append("Dislikes: ").append(recipient.getDislikes()).append('\n');
        }
        if (recipient.getPersonalityStyle() != null) {
            context.append("Personality: ").append(recipient.getPersonalityStyle()).append('\n');
        }
        context.append("Occasion: ").append(giftPlan.getOccasionType()).append('\n');
        if (giftPlan.getOccasionDate() != null) {
            context.append("Occasion date: ").append(giftPlan.getOccasionDate()).append('\n');
        }
        context.append("Budget: ").append(giftPlan.getBudget()).append('\n');
        context.append("Currency: ").append(giftPlan.getCurrency()).append('\n');
        if (giftPlan.getPreferredGiftStyle() != null) {
            context.append("Preferred gift style: ").append(giftPlan.getPreferredGiftStyle()).append('\n');
        }

        if (!requiredAnswers.isEmpty()) {
            context.append("\nRequired question answers:\n");
            for (RequiredQuestionAnswer answer : requiredAnswers) {
                context.append("- ")
                        .append(answer.getRequiredQuestion().getQuestionText())
                        .append(": ")
                        .append(answer.getAnswerText())
                        .append('\n');
            }
        }

        return """
                Generate follow-up gift questions for the context below helping the user choose a better gift for the recipient.
                Return JSON only in this exact shape:
                {
                  "questions": [
                    {
                      "questionText": "string",
                      "reasonForQuestion": "string"
                    }
                  ]
                }

                Rules:
                - Generate 3 to 5 questions
                - Questions must help choose a better gift for this recipient and occasion
                - Avoid generic questions
                - the response should be in Arabic language

                Context:
                %s
                """.formatted(context);
    }

    private AiGeneratedQuestionDTOOut toQuestionDto(AiGeneratedQuestion question) {
        return new AiGeneratedQuestionDTOOut(
                question.getId(),
                question.getGiftPlan().getId(),
                question.getQuestionText(),
                question.getReasonForQuestion(),
                question.getDisplayOrder(),
                question.getCreatedAt()
        );
    }

}
