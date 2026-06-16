package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.DTO.IN.GiftHistoryCreateDTOIn;
import org.example.tahadaw.DTO.IN.GiftHistoryUpdateDTOIn;
import org.example.tahadaw.DTO.OUT.GiftHistoryDTOOut;
import org.example.tahadaw.Model.GiftHistory;
import org.example.tahadaw.Model.GiftIdeaRecommendation;
import org.example.tahadaw.Model.GiftPlan;
import org.example.tahadaw.Model.Recipient;
import org.example.tahadaw.Model.SelectedProduct;
import org.example.tahadaw.Model.User;
import org.example.tahadaw.Model.enums.GiftPlanStatus;
import org.example.tahadaw.Repository.GiftHistoryRepository;
import org.example.tahadaw.Repository.GiftIdeaRecommendationRepository;
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
public class GiftHistoryService {

    private final GiftHistoryRepository giftHistoryRepository;
    private final UserRepository userRepository;
    private final RecipientRepository recipientRepository;
    private final GiftPlanRepository giftPlanRepository;
    private final GiftIdeaRecommendationRepository giftIdeaRecommendationRepository;

    @Transactional
    public GiftHistoryDTOOut saveFromPlan(Long userId, Long giftPlanId) {
        GiftPlan giftPlan = requireOwnedGiftPlan(userId, giftPlanId);
        GiftIdeaRecommendation selectedIdea = giftIdeaRecommendationRepository
                .findByGiftPlanAndIsSelectedTrue(giftPlan)
                .orElseThrow(() -> new ApiException("Select one AI gift idea before saving gift history."));

        if (giftHistoryRepository.existsByGiftIdeaRecommendation_Id(selectedIdea.getId())) {
            throw new ApiException("Gift history already saved for this gift plan.");
        }

        SelectedProduct selectedProduct = selectedIdea.getSelectedProduct();
        String giftName = selectedProduct != null ? selectedProduct.getTitle() : selectedIdea.getTitle();
        Long priceMinor = selectedProduct != null ? selectedProduct.getPriceMinor() : null;

        GiftHistory history = new GiftHistory();
        history.setUser(giftPlan.getUser());
        history.setRecipient(giftPlan.getRecipient());
        history.setGiftIdeaRecommendation(selectedIdea);
        history.setGiftName(giftName);
        history.setOccasionType(giftPlan.getOccasionType());
        history.setGiftDate(giftPlan.getOccasionDate() != null ? giftPlan.getOccasionDate() : LocalDate.now());
        history.setPriceMinor(priceMinor);
        history.setCreatedAt(LocalDateTime.now());

        giftPlan.setStatus(GiftPlanStatus.COMPLETED);
        giftPlan.setUpdatedAt(LocalDateTime.now());
        giftPlanRepository.save(giftPlan);

        return toDto(giftHistoryRepository.save(history));
    }

    public List<GiftHistoryDTOOut> listByRecipient(Long userId, Long recipientId) {
        requireOwnedRecipient(userId, recipientId);

        return giftHistoryRepository.findByRecipient_IdAndUser_IdOrderByCreatedAtDesc(recipientId, userId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public GiftHistoryDTOOut create(Long userId, GiftHistoryCreateDTOIn request) {
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new ApiException("User not found."));
        Recipient recipient = requireOwnedRecipient(userId, request.getRecipientId());

        GiftHistory history = new GiftHistory();
        history.setUser(user);
        history.setRecipient(recipient);
        history.setGiftName(request.getGiftName());
        history.setOccasionType(request.getOccasionType());
        history.setGiftDate(request.getGiftDate());
        history.setPriceMinor(request.getPriceMinor());
        history.setWasGifted(request.getWasGifted());
        history.setUserRating(request.getUserRating());
        history.setNotes(request.getNotes());
        history.setCreatedAt(LocalDateTime.now());

        if (request.getGiftIdeaRecommendationId() != null) {
            GiftIdeaRecommendation recommendation = giftIdeaRecommendationRepository
                    .findGiftIdeaRecommendationById(request.getGiftIdeaRecommendationId())
                    .orElseThrow(() -> new ApiException("Gift idea recommendation not found."));
            validateHistoryRecommendation(user, recipient, recommendation);
            if (giftHistoryRepository.existsByGiftIdeaRecommendation_Id(recommendation.getId())) {
                throw new ApiException("Gift history already exists for this gift idea.");
            }
            history.setGiftIdeaRecommendation(recommendation);
        }

        return toDto(giftHistoryRepository.save(history));
    }

    public List<GiftHistoryDTOOut> listMine(Long userId) {
        userRepository.findUserById(userId)
                .orElseThrow(() -> new ApiException("User not found."));

        return giftHistoryRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDto)
                .toList();
    }

    public GiftHistoryDTOOut getOne(Long userId, Long historyId) {
        return toDto(requireOwnedHistory(userId, historyId));
    }

    @Transactional
    public GiftHistoryDTOOut update(Long userId, Long historyId, GiftHistoryUpdateDTOIn request) {
        GiftHistory history = requireOwnedHistory(userId, historyId);

        if (request.getGiftName() != null) {
            history.setGiftName(request.getGiftName());
        }
        if (request.getOccasionType() != null) {
            history.setOccasionType(request.getOccasionType());
        }
        if (request.getGiftDate() != null) {
            history.setGiftDate(request.getGiftDate());
        }
        if (request.getPriceMinor() != null) {
            history.setPriceMinor(request.getPriceMinor());
        }
        if (request.getWasGifted() != null) {
            history.setWasGifted(request.getWasGifted());
        }
        if (request.getUserRating() != null) {
            history.setUserRating(request.getUserRating());
        }
        if (request.getNotes() != null) {
            history.setNotes(request.getNotes());
        }

        if (Boolean.TRUE.equals(request.getWasGifted())) {
            completeLinkedGiftPlan(history);
        }

        return toDto(giftHistoryRepository.save(history));
    }

    @Transactional
    public void delete(Long userId, Long historyId) {
        GiftHistory history = requireOwnedHistory(userId, historyId);
        giftHistoryRepository.delete(history);
    }

    private GiftPlan requireOwnedGiftPlan(Long userId, Long giftPlanId) {
        GiftPlan giftPlan = giftPlanRepository.findGiftPlanById(giftPlanId)
                .orElseThrow(() -> new ApiException("Gift plan not found."));
        if (!giftPlan.getUser().getId().equals(userId)) {
            throw new ApiException("Gift plan not found.");
        }
        return giftPlan;
    }

    private Recipient requireOwnedRecipient(Long userId, Long recipientId) {
        Recipient recipient = recipientRepository.findRecipientById(recipientId)
                .orElseThrow(() -> new ApiException("Recipient not found."));
        if (!recipient.getUser().getId().equals(userId)) {
            throw new ApiException("Recipient not found.");
        }
        return recipient;
    }

    private GiftHistory requireOwnedHistory(Long userId, Long historyId) {
        GiftHistory history = giftHistoryRepository.findGiftHistoryById(historyId)
                .orElseThrow(() -> new ApiException("Gift history not found."));
        if (!history.getUser().getId().equals(userId)) {
            throw new ApiException("Gift history not found.");
        }
        return history;
    }

    private void validateHistoryRecommendation(User user, Recipient recipient, GiftIdeaRecommendation recommendation) {
        if (!Boolean.TRUE.equals(recommendation.getIsSelected())) {
            throw new ApiException("Gift history can only link to the selected gift idea.");
        }
        GiftPlan plan = recommendation.getGiftPlan();
        if (!plan.getUser().getId().equals(user.getId())) {
            throw new ApiException("Gift idea recommendation not found.");
        }
        if (!plan.getRecipient().getId().equals(recipient.getId())) {
            throw new ApiException("Gift history recipient must match the linked gift plan recipient.");
        }
    }

    private void completeLinkedGiftPlan(GiftHistory history) {
        if (history.getGiftIdeaRecommendation() == null) {
            return;
        }
        GiftPlan giftPlan = history.getGiftIdeaRecommendation().getGiftPlan();
        if (giftPlan.getStatus() != GiftPlanStatus.COMPLETED) {
            giftPlan.setStatus(GiftPlanStatus.COMPLETED);
            giftPlan.setUpdatedAt(LocalDateTime.now());
            giftPlanRepository.save(giftPlan);
        }
    }

    private GiftHistoryDTOOut toDto(GiftHistory history) {
        return new GiftHistoryDTOOut(
                history.getId(),
                history.getUser().getId(),
                history.getRecipient().getId(),
                history.getGiftIdeaRecommendation() != null ? history.getGiftIdeaRecommendation().getId() : null,
                history.getGiftName(),
                history.getOccasionType(),
                history.getGiftDate(),
                history.getPriceMinor(),
                history.getWasGifted(),
                history.getUserRating(),
                history.getNotes(),
                history.getCreatedAt()
        );
    }
}
