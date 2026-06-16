package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.DTO.IN.RequiredQuestionAnswerItemDTOIn;
import org.example.tahadaw.DTO.IN.RequiredQuestionAnswersSubmitDTOIn;
import org.example.tahadaw.DTO.OUT.RequiredQuestionAnswerDTOOut;
import org.example.tahadaw.Model.GiftPlan;
import org.example.tahadaw.Model.RequiredQuestion;
import org.example.tahadaw.Model.RequiredQuestionAnswer;
import org.example.tahadaw.Model.enums.GiftPlanStatus;
import org.example.tahadaw.Repository.GiftPlanRepository;
import org.example.tahadaw.Repository.RequiredQuestionAnswerRepository;
import org.example.tahadaw.Repository.RequiredQuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RequiredQuestionAnswerService {

    private final RequiredQuestionAnswerRepository requiredQuestionAnswerRepository;
    private final RequiredQuestionRepository requiredQuestionRepository;
    private final GiftPlanRepository giftPlanRepository;

    @Transactional
    public List<RequiredQuestionAnswerDTOOut> submitAnswers(Long userId, Long giftPlanId,
                                                            RequiredQuestionAnswersSubmitDTOIn request) {
        GiftPlan giftPlan = requireOwnedGiftPlan(userId, giftPlanId);

        if (giftPlan.getStatus() != GiftPlanStatus.CREATED) {
            throw new ApiException("Required answers can only be submitted while the gift plan is in CREATED status.");
        }

        List<RequiredQuestion> activeQuestions = requiredQuestionRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
        if (activeQuestions.isEmpty()) {
            throw new ApiException("No active required questions are configured.");
        }

        Set<Long> submittedQuestionIds = new HashSet<>();
        for (RequiredQuestionAnswerItemDTOIn item : request.getAnswers()) {
            if (!submittedQuestionIds.add(item.getRequiredQuestionId())) {
                throw new ApiException("Duplicate answer for required question id " + item.getRequiredQuestionId() + ".");
            }
        }

        for (RequiredQuestion question : activeQuestions) {
            if (!submittedQuestionIds.contains(question.getId())) {
                throw new ApiException("Answer required for question: " + question.getQuestionText());
            }
        }

        for (RequiredQuestionAnswerItemDTOIn item : request.getAnswers()) {
            RequiredQuestion question = requiredQuestionRepository.findRequiredQuestionById(item.getRequiredQuestionId())
                    .orElseThrow(() -> new ApiException("Required question not found."));
            if (!Boolean.TRUE.equals(question.getIsActive())) {
                throw new ApiException("Required question is not active: " + question.getId());
            }

            RequiredQuestionAnswer answer = requiredQuestionAnswerRepository
                    .findByGiftPlan_IdAndRequiredQuestion_Id(giftPlanId, question.getId())
                    .orElseGet(RequiredQuestionAnswer::new);

            answer.setGiftPlan(giftPlan);
            answer.setRequiredQuestion(question);
            answer.setAnswerText(item.getAnswerText());
            if (answer.getCreatedAt() == null) {
                answer.setCreatedAt(LocalDateTime.now());
            }
            requiredQuestionAnswerRepository.save(answer);
        }

        giftPlan.setStatus(GiftPlanStatus.REQUIRED_QUESTIONS_ANSWERED);
        giftPlan.setUpdatedAt(LocalDateTime.now());
        giftPlanRepository.save(giftPlan);

        return listByGiftPlan(userId, giftPlanId);
    }

    public List<RequiredQuestionAnswerDTOOut> listByGiftPlan(Long userId, Long giftPlanId) {
        requireOwnedGiftPlan(userId, giftPlanId);

        return requiredQuestionAnswerRepository.findByGiftPlan_IdOrderByCreatedAtAsc(giftPlanId).stream()
                .map(this::toDto)
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

    private RequiredQuestionAnswerDTOOut toDto(RequiredQuestionAnswer answer) {
        return new RequiredQuestionAnswerDTOOut(
                answer.getId(),
                answer.getGiftPlan().getId(),
                answer.getRequiredQuestion().getId(),
                answer.getRequiredQuestion().getQuestionText(),
                answer.getAnswerText(),
                answer.getCreatedAt()
        );
    }
}
