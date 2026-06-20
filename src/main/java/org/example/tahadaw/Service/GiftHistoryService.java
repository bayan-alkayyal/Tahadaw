package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.DTO.IN.GiftHistoryLogDTOIn;
import org.example.tahadaw.DTO.OUT.GiftHistoryDTOOut;
import org.example.tahadaw.DTO.OUT.GiftHistorySummaryDTOOut;
import org.example.tahadaw.Model.GiftHistory;
import org.example.tahadaw.Model.GiftPlan;
import org.example.tahadaw.Model.Recipient;
import org.example.tahadaw.Model.SelectedProduct;
import org.example.tahadaw.Repository.GiftHistoryRepository;
import org.example.tahadaw.Repository.GiftPlanRepository;
import org.example.tahadaw.Repository.RecipientRepository;
import org.example.tahadaw.Repository.SelectedProductRepository;
import org.example.tahadaw.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GiftHistoryService {

    private final GiftHistoryRepository giftHistoryRepository;
    private final UserRepository userRepository;
    private final RecipientRepository recipientRepository;
    private final GiftPlanRepository giftPlanRepository;
    private final SelectedProductRepository selectedProductRepository;

    public List<GiftHistoryDTOOut> listByRecipient(Long userId, Long recipientId) {
        requireOwnedRecipient(userId, recipientId);

        return selectedProductRepository.findByRecipient_IdAndIsSelectedTrueOrderByCreatedAtDesc(recipientId).stream()
                .map(this::toDtoFromProduct)
                .toList();
    }

    public List<GiftHistoryDTOOut> listMine(Long userId) {
        userRepository.findUserById(userId)
                .orElseThrow(() -> new ApiException("User not found."));

        return selectedProductRepository.findByUser_IdAndIsSelectedTrueOrderByCreatedAtDesc(userId).stream()
                .map(this::toDtoFromProduct)
                .toList();
    }

    /**
     * Attach user-written history fields to an already-selected product (create once).
     */
    @Transactional
    public GiftHistoryDTOOut logFromProduct(Long userId, Long selectedProductId, GiftHistoryLogDTOIn request) {
        SelectedProduct product = requireOwnedSelectedProduct(userId, selectedProductId);

        if (giftHistoryRepository.existsBySelectedProduct_Id(product.getId())) {
            throw new ApiException("Gift history already logged for this product. Use the edit endpoint instead.");
        }

        GiftPlan giftPlan = product.getGiftPlan();

        GiftHistory history = new GiftHistory();
        history.setUser(product.getUser() != null ? product.getUser() : giftPlan.getUser());
        history.setRecipient(product.getRecipient() != null ? product.getRecipient() : giftPlan.getRecipient());
        history.setSelectedProduct(product);
        history.setGiftName(product.getProductName());
        history.setOccasionType(giftPlan != null ? giftPlan.getOccasionType() : null);
        history.setGiftDate(giftPlan != null && giftPlan.getOccasionDate() != null
                ? giftPlan.getOccasionDate() : LocalDate.now());
        history.setPriceMinor(product.getPrice());
        // Logging from a selected product means the gift was actually given.
        history.setWasGifted(true);
        history.setUserRating(request.getUserRating());
        history.setNotes(request.getNotes());
        history.setCreatedAt(LocalDateTime.now());

        if (giftPlan != null) {
            completeGiftPlan(giftPlan);
        }

        GiftHistory saved = giftHistoryRepository.save(history);
        product.setGiftHistory(saved);
        return toDtoFromProduct(product);
    }

    /**
     * Edit the history fields previously logged for a selected product.
     */
    @Transactional
    public GiftHistoryDTOOut editLog(Long userId, Long selectedProductId, GiftHistoryLogDTOIn request) {
        SelectedProduct product = requireOwnedSelectedProduct(userId, selectedProductId);
        GiftHistory history = giftHistoryRepository.findBySelectedProduct_Id(product.getId())
                .orElseThrow(() -> new ApiException("No gift history logged for this product yet."));

        if (request.getWasGifted() != null) {
            history.setWasGifted(request.getWasGifted());
        }
        if (request.getUserRating() != null) {
            history.setUserRating(request.getUserRating());
        }
        if (request.getNotes() != null) {
            history.setNotes(request.getNotes());
        }

        if (Boolean.TRUE.equals(request.getWasGifted()) && product.getGiftPlan() != null) {
            completeGiftPlan(product.getGiftPlan());
        }

        giftHistoryRepository.save(history);
        product.setGiftHistory(history);
        return toDtoFromProduct(product);
    }

    @Transactional
    public void deleteLog(Long userId, Long selectedProductId) {
        SelectedProduct product = requireOwnedSelectedProduct(userId, selectedProductId);
        GiftHistory history = giftHistoryRepository.findBySelectedProduct_Id(product.getId())
                .orElseThrow(() -> new ApiException("No gift history logged for this product yet."));
        giftHistoryRepository.delete(history);
    }

    public GiftHistoryDTOOut getByProduct(Long userId, Long selectedProductId) {
        SelectedProduct product = requireOwnedSelectedProduct(userId, selectedProductId);
        return toDtoFromProduct(product);
    }

    public GiftHistorySummaryDTOOut summary(Long userId) {
        userRepository.findUserById(userId)
                .orElseThrow(() -> new ApiException("User not found."));

        List<SelectedProduct> all = selectedProductRepository
                .findByUser_IdAndIsSelectedTrueOrderByCreatedAtDesc(userId);

        double totalSpent = all.stream()
                .filter(p -> p.getPrice() != null)
                .mapToDouble(SelectedProduct::getPrice)
                .sum();
        long total = all.size();

        Map<String, Long> byOccasion = all.stream().collect(Collectors.groupingBy(
                p -> p.getGiftPlan() != null
                        && p.getGiftPlan().getOccasionType() != null
                        && !p.getGiftPlan().getOccasionType().isBlank()
                        ? p.getGiftPlan().getOccasionType() : "UNKNOWN",
                Collectors.counting()));
        Map<String, Long> byRecipient = all.stream().collect(Collectors.groupingBy(
                p -> p.getRecipient() != null && p.getRecipient().getName() != null
                        ? p.getRecipient().getName() : "UNKNOWN",
                Collectors.counting()));

        return new GiftHistorySummaryDTOOut(total, totalSpent, total, 0, byOccasion, byRecipient);
    }

    private Recipient requireOwnedRecipient(Long userId, Long recipientId) {
        Recipient recipient = recipientRepository.findRecipientById(recipientId)
                .orElseThrow(() -> new ApiException("Recipient not found."));
        if (!recipient.getUser().getId().equals(userId)) {
            throw new ApiException("Recipient not found.");
        }
        return recipient;
    }

    private SelectedProduct requireOwnedSelectedProduct(Long userId, Long selectedProductId) {
        SelectedProduct product = selectedProductRepository.findSelectedProductById(selectedProductId);
        if (product == null) {
            throw new ApiException("Selected product not found.");
        }
        Long ownerId = product.getUser() != null
                ? product.getUser().getId()
                : (product.getGiftPlan() != null && product.getGiftPlan().getUser() != null
                        ? product.getGiftPlan().getUser().getId() : null);
        if (ownerId == null || !ownerId.equals(userId)) {
            throw new ApiException("Selected product not found.");
        }
        if (!Boolean.TRUE.equals(product.getIsSelected())) {
            throw new ApiException("This product is not selected for any gift plan.");
        }
        return product;
    }

    private void completeGiftPlan(GiftPlan giftPlan) {
        if (!"COMPLETED".equals(giftPlan.getStatus())) {
            giftPlan.setStatus("COMPLETED");
            giftPlan.setUpdatedAt(LocalDateTime.now());
            giftPlanRepository.save(giftPlan);
        }
    }

    private GiftHistoryDTOOut toDtoFromProduct(SelectedProduct product) {
        GiftPlan giftPlan = product.getGiftPlan();
        GiftHistory log = product.getGiftHistory();
        return new GiftHistoryDTOOut(
                product.getId(),
                product.getUser() != null ? product.getUser().getId() : null,
                product.getRecipient() != null ? product.getRecipient().getId() : null,
                product.getGiftIdeaRecommendation() != null ? product.getGiftIdeaRecommendation().getId() : null,
                product.getProductName(),
                giftPlan != null ? giftPlan.getOccasionType() : null,
                giftPlan != null ? giftPlan.getOccasionDate() : null,
                product.getPrice(),
                log != null ? log.getWasGifted() : null,
                log != null ? log.getUserRating() : null,
                log != null ? log.getNotes() : null,
                log != null && log.getCreatedAt() != null ? log.getCreatedAt() : product.getCreatedAt()
        );
    }
}
