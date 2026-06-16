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
import org.example.tahadaw.Model.enums.GiftPlanStatus;
import org.example.tahadaw.Repository.AiGeneratedQuestionRepository;
import org.example.tahadaw.Repository.AiQuestionAnswerRepository;
import org.example.tahadaw.Repository.GiftPlanRepository;
import org.example.tahadaw.Repository.RequiredQuestionAnswerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        aiGeneratedQuestion.setDisplayOrder(request.getDisplayOrder());
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
        oldAiGeneratedQuestion.setDisplayOrder(request.getDisplayOrder());
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

    // gift-plan flow

    @Transactional
    public List<AiGeneratedQuestionDTOOut> generateQuestions(Long userId, Long giftPlanId) {
        GiftPlan giftPlan = requireOwnedGiftPlan(userId, giftPlanId);

        if (giftPlan.getStatus() != GiftPlanStatus.REQUIRED_QUESTIONS_ANSWERED) {
            throw new ApiException("Answer all required questions before generating AI follow-up questions.");
        }
        if (aiGeneratedQuestionRepository.existsByGiftPlan_Id(giftPlanId)) {
            throw new ApiException("AI follow-up questions already generated for this gift plan.");
        }

        String prompt = buildGeneratePrompt(giftPlan);
        JsonNode root = AiJsonParser.parseObject(aiService.ask(prompt));
        JsonNode questionsNode = root.get("questions");
        if (questionsNode == null || !questionsNode.isArray() || questionsNode.isEmpty()) {
            throw new ApiException("AI did not return any follow-up questions.");
        }

        LocalDateTime now = LocalDateTime.now();
        for (JsonNode questionNode : questionsNode) {
            AiGeneratedQuestion question = new AiGeneratedQuestion();
            question.setGiftPlan(giftPlan);
            question.setQuestionText(AiJsonParser.requireText(questionNode, "questionText"));
            question.setReasonForQuestion(AiJsonParser.requireText(questionNode, "reasonForQuestion"));
            question.setDisplayOrder(AiJsonParser.requireInt(questionNode, "displayOrder", 1, 20));
            question.setCreatedAt(now);
            aiGeneratedQuestionRepository.save(question);
        }

        giftPlan.setStatus(GiftPlanStatus.AI_QUESTIONS_GENERATED);
        giftPlan.setUpdatedAt(now);
        giftPlanRepository.save(giftPlan);

        return listQuestions(userId, giftPlanId);
    }

    public List<AiGeneratedQuestionDTOOut> listQuestions(Long userId, Long giftPlanId) {
        requireOwnedGiftPlan(userId, giftPlanId);

        return aiGeneratedQuestionRepository.findByGiftPlan_IdOrderByDisplayOrderAsc(giftPlanId).stream()
                .map(this::toQuestionDto)
                .toList();
    }

    @Transactional
    public List<AiQuestionAnswerDTOOut> submitAnswers(Long userId, Long giftPlanId,
                                                    AiQuestionAnswersSubmitDTOIn request) {
        GiftPlan giftPlan = requireOwnedGiftPlan(userId, giftPlanId);

        if (giftPlan.getStatus() != GiftPlanStatus.AI_QUESTIONS_GENERATED) {
            throw new ApiException("Generate AI follow-up questions before submitting answers.");
        }

        List<AiGeneratedQuestion> planQuestions =
                aiGeneratedQuestionRepository.findByGiftPlan_IdOrderByDisplayOrderAsc(giftPlanId);
        if (planQuestions.isEmpty()) {
            throw new ApiException("No AI follow-up questions found for this gift plan.");
        }

        Set<Long> submittedQuestionIds = new HashSet<>();
        for (AiQuestionAnswerItemDTOIn item : request.getAnswers()) {
            if (!submittedQuestionIds.add(item.getAiGeneratedQuestionId())) {
                throw new ApiException("Duplicate answer for AI question id " + item.getAiGeneratedQuestionId() + ".");
            }
        }

        for (AiGeneratedQuestion question : planQuestions) {
            if (!submittedQuestionIds.contains(question.getId())) {
                throw new ApiException("Answer required for AI question: " + question.getQuestionText());
            }
        }

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        for (AiQuestionAnswerItemDTOIn item : request.getAnswers()) {
            AiGeneratedQuestion question = aiGeneratedQuestionRepository
                    .findAiGeneratedQuestionById(item.getAiGeneratedQuestionId())
                    .orElseThrow(() -> new ApiException("AI question not found."));
            if (!question.getGiftPlan().getId().equals(giftPlanId)) {
                throw new ApiException("AI question does not belong to this gift plan.");
            }

            AiQuestionAnswer answer = aiQuestionAnswerRepository
                    .findByAiGeneratedQuestion_Id(question.getId())
                    .orElseGet(AiQuestionAnswer::new);

            answer.setAiGeneratedQuestion(question);
            answer.setAnswerText(item.getAnswerText());
            if (answer.getCreatedAt() == null) {
                answer.setCreatedAt(today);
            }
            aiQuestionAnswerRepository.save(answer);
        }

        giftPlan.setStatus(GiftPlanStatus.AI_QUESTIONS_ANSWERED);
        giftPlan.setUpdatedAt(now);
        giftPlanRepository.save(giftPlan);

        return listAnswers(userId, giftPlanId);
    }

    public List<AiQuestionAnswerDTOOut> listAnswers(Long userId, Long giftPlanId) {
        requireOwnedGiftPlan(userId, giftPlanId);

        return aiQuestionAnswerRepository.findByAiGeneratedQuestion_GiftPlan_IdOrderByCreatedAtAsc(giftPlanId).stream()
                .map(this::toAnswerDto)
                .toList();
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

    private String buildGeneratePrompt(GiftPlan giftPlan) {
        Recipient recipient = giftPlan.getRecipient();
        List<RequiredQuestionAnswer> requiredAnswers =
                requiredQuestionAnswerRepository.findByGiftPlan_IdOrderByCreatedAtAsc(giftPlan.getId());

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
        context.append("Budget minor units: ").append(giftPlan.getBudgetMinor()).append('\n');
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
                Generate tailored follow-up gift questions for the context below.
                Return JSON only in this exact shape:
                {
                  "questions": [
                    {
                      "questionText": "string",
                      "reasonForQuestion": "string",
                      "displayOrder": 1
                    }
                  ]
                }

                Rules:
                - Generate 3 to 5 questions
                - Questions must help choose a better gift for this recipient and occasion
                - Avoid generic questions
                - displayOrder starts at 1 and increases by 1

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

    private AiQuestionAnswerDTOOut toAnswerDto(AiQuestionAnswer answer) {
        return new AiQuestionAnswerDTOOut(
                answer.getId(),
                answer.getAiGeneratedQuestion().getId(),
                answer.getAiGeneratedQuestion().getQuestionText(),
                answer.getAnswerText(),
                answer.getCreatedAt()
        );
    }
}
