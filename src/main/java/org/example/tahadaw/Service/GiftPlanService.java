package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.DTO.IN.GiftPlanDTOIn;
import org.example.tahadaw.DTO.OUT.GiftPlanSummeryDTOOut;
import org.example.tahadaw.DTO.OUT.RecipientDTOOut;
import org.example.tahadaw.DTO.OUT.SelectedProductSummeryDTOOut;
import org.example.tahadaw.Model.*;
import org.example.tahadaw.Repository.GiftMessageRepository;
import org.example.tahadaw.Repository.GiftPlanRepository;
import org.example.tahadaw.Repository.RecipientRepository;
import org.example.tahadaw.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GiftPlanService {

    private final GiftPlanRepository giftPlanRepository;
    private final UserRepository userRepository;
    private final RecipientRepository recipientRepository;
    private final GiftMessageRepository giftMessageRepository;

    public List<GiftPlan> listByUser(Long userId) {
        userRepository.findUserById(userId)
                .orElseThrow(() -> new ApiException("User not found."));

        return giftPlanRepository.findByUser_IdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public GiftPlan createGiftPlan(Long userId, Long recipientId, GiftPlanDTOIn request) {
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new ApiException("User not found."));
        Recipient recipient = requireOwnedRecipient(userId, recipientId);

        LocalDateTime now = LocalDateTime.now();
        GiftPlan giftPlan = new GiftPlan();
        giftPlan.setOccasionType(request.getOccasionType());
        giftPlan.setOccasionDate(request.getOccasionDate());
        giftPlan.setBudget(request.getBudget());
        giftPlan.setCurrency("SAR");
        giftPlan.setPreferredGiftStyle(request.getPreferredGiftStyle());
        giftPlan.setLanguage(request.getLanguage());
        giftPlan.setStatus("CREATED");
        giftPlan.setUser(user);
        giftPlan.setRecipient(recipient);
        giftPlan.setCreatedAt(now);
        giftPlan.setUpdatedAt(now);

        return giftPlanRepository.save(giftPlan);
    }

    @Transactional
    public GiftPlan updateGiftPlan(Long userId, Long id, GiftPlanDTOIn request) {
        GiftPlan giftPlan = requireOwnedGiftPlan(userId, id);
        giftPlan.setOccasionType(request.getOccasionType());
        giftPlan.setOccasionDate(request.getOccasionDate());
        giftPlan.setBudget(request.getBudget());
        giftPlan.setCurrency(request.getCurrency());
        giftPlan.setPreferredGiftStyle(request.getPreferredGiftStyle());
        giftPlan.setLanguage(request.getLanguage());
        giftPlan.setUpdatedAt(LocalDateTime.now());
        return giftPlanRepository.save(giftPlan);
    }

    @Transactional
    public void deleteGiftPlan(Long userId, Long id) {
        GiftPlan giftPlan = requireOwnedGiftPlan(userId, id);
        giftPlanRepository.delete(giftPlan);
    }

    public List<GiftPlan> listAllActiveGiftPlans(Long userId) {
        List<GiftPlan> activePlan=giftPlanRepository.findGiftPlanByUserIdAndOccasionDateAfter(userId, LocalDate.now());
        if (activePlan.isEmpty())
            throw new ApiException("No active gift plans found.");
        return activePlan;

    }
    public List<GiftPlan> listAllPreviousGiftPlans(Long userId) {
        List<GiftPlan> previousPlan=giftPlanRepository.findGiftPlanByUserIdAndOccasionDateBefore(userId, LocalDate.now());
        if (previousPlan.isEmpty())
            throw new ApiException("No previous gift plans found.");
        return previousPlan;
    }

    public GiftPlanSummeryDTOOut getGiftPlanSummary(Long userId,Long giftPlanId) {
        GiftPlan giftPlan = giftPlanRepository.findGiftPlanById(giftPlanId)
                .orElseThrow(() -> new ApiException("Gift plan not found."));
        if (!giftPlan.getUser().getId().equals(userId)) {
            throw new ApiException("Gift plan not yours.");
        }

        RecipientDTOOut recipientDto = new RecipientDTOOut(
                giftPlan.getRecipient().getName(),
                giftPlan.getRecipient().getRelationship()
        );

        SelectedProductSummeryDTOOut selectedProductDto = null;
        GiftIdeaRecommendation selectedIdea = giftPlan.getSelectedGiftIdea();
        if (selectedIdea != null
                && selectedIdea.getSelectedProducts() != null
                && !selectedIdea.getSelectedProducts().isEmpty()) {
            SelectedProduct product = selectedIdea.getSelectedProducts().iterator().next();
            selectedProductDto = new SelectedProductSummeryDTOOut(
                    product.getProductName(),
                    product.getPrice(),
                    product.getStoreName()
            );
        }

        String message = giftMessageRepository
                .findFirstByGiftPlan_IdOrderByCreatedAtDesc(giftPlan.getId())
                .map(GiftMessage::getMessageText)
                .orElse(null);

        return new GiftPlanSummeryDTOOut(
                giftPlan.getId(),
                giftPlan.getOccasionType(),
                giftPlan.getOccasionDate(),
                recipientDto,
                giftPlan.getBudget(),
                selectedProductDto,
                message
        );
    }

    public GiftPlan getGiftPlanById(Long userId, Long id) {
        return requireOwnedGiftPlan(userId, id);
    }

    private Recipient requireOwnedRecipient(Long userId, Long recipientId) {
        Recipient recipient = recipientRepository.findRecipientById(recipientId)
                .orElseThrow(() -> new ApiException("Recipient not found."));
        if (!recipient.getUser().getId().equals(userId)) {
            throw new ApiException("Recipient not yours.");
        }
        return recipient;
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
}
