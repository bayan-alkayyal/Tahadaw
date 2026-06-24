package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.DTO.IN.GiftHistoryLogDTOIn;
import org.example.tahadaw.DTO.OUT.GiftHistoryDTOOut;
import org.example.tahadaw.Mapper.ResponseMapper;
import org.example.tahadaw.DTO.OUT.GiftHistorySummaryDTOOut;
import org.example.tahadaw.DTO.OUT.RecipientInsightsDTOOut;
import org.example.tahadaw.DTO.OUT.SpendingStatsDTOOut;
import org.example.tahadaw.Model.GiftHistory;
import org.example.tahadaw.Model.GiftPlan;
import org.example.tahadaw.Model.GiftPlanStatus;
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
import java.util.Comparator;
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

        return giftHistoryRepository
                .findByRecipient_IdAndUser_IdAndWasGiftedTrueOrderByCreatedAtDesc(recipientId, userId).stream()
                .map(ResponseMapper::toGiftHistoryDto)
                .toList();
    }

    public List<GiftHistoryDTOOut> listMine(Long userId) {
        userRepository.findUserById(userId)
                .orElseThrow(() -> new ApiException("User not found."));

        return giftHistoryRepository.findByUser_IdAndWasGiftedTrueOrderByCreatedAtDesc(userId).stream()
                .map(ResponseMapper::toGiftHistoryDto)
                .toList();
    }

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
        history.setWasGifted(true);
        history.setUserRating(request.getUserRating());
        history.setNotes(request.getNotes());
        history.setCreatedAt(LocalDateTime.now());

        if (giftPlan != null) {
            completeGiftPlan(giftPlan);
        }

        GiftHistory saved = giftHistoryRepository.save(history);
        product.setGiftHistory(saved);
        return ResponseMapper.toGiftHistoryDto(saved);
    }

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
        return ResponseMapper.toGiftHistoryDto(history);
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
        GiftHistory history = giftHistoryRepository.findBySelectedProduct_Id(product.getId()).orElse(null);
        if (history != null) {
            return ResponseMapper.toGiftHistoryDto(history);
        }
        return ResponseMapper.toGiftHistoryDto(product);
    }

    public GiftHistorySummaryDTOOut summary(Long userId) {
        userRepository.findUserById(userId)
                .orElseThrow(() -> new ApiException("User not found."));

        List<GiftHistory> gifted = giftHistoryRepository
                .findByUser_IdAndWasGiftedTrueOrderByCreatedAtDesc(userId);

        double totalSpent = gifted.stream()
                .filter(h -> h.getPriceMinor() != null)
                .mapToDouble(GiftHistory::getPriceMinor)
                .sum();
        long total = gifted.size();

        Map<String, Long> byOccasion = gifted.stream().collect(Collectors.groupingBy(
                GiftHistoryService::occasionOf, Collectors.counting()));
        Map<String, Long> byRecipient = gifted.stream().collect(Collectors.groupingBy(
                h -> h.getRecipient() != null && h.getRecipient().getName() != null
                        ? h.getRecipient().getName() : "UNKNOWN",
                Collectors.counting()));

        return new GiftHistorySummaryDTOOut(total, totalSpent, byOccasion, byRecipient);
    }

    public SpendingStatsDTOOut spendingStats(Long userId, LocalDate from, LocalDate to) {
        userRepository.findUserById(userId)
                .orElseThrow(() -> new ApiException("User not found."));

        List<GiftHistory> filtered = giftHistoryRepository
                .findByUser_IdAndWasGiftedTrueOrderByCreatedAtDesc(userId).stream()
                .filter(history -> withinWindow(effectiveDate(history), from, to))
                .toList();

        double totalSpent = filtered.stream()
                .filter(history -> history.getPriceMinor() != null)
                .mapToDouble(GiftHistory::getPriceMinor)
                .sum();
        long giftCount = filtered.size();
        double averagePerGift = giftCount > 0 ? totalSpent / giftCount : 0.0;

        Map<String, Double> spentByRecipient = filtered.stream().collect(Collectors.groupingBy(
                history -> history.getRecipient() != null && history.getRecipient().getName() != null
                        ? history.getRecipient().getName() : "UNKNOWN",
                Collectors.summingDouble(history -> history.getPriceMinor() != null ? history.getPriceMinor() : 0.0)));
        Map<String, Double> spentByOccasion = filtered.stream().collect(Collectors.groupingBy(
                GiftHistoryService::occasionOf,
                Collectors.summingDouble(history -> history.getPriceMinor() != null ? history.getPriceMinor() : 0.0)));

        return new SpendingStatsDTOOut(from, to, giftCount, totalSpent, averagePerGift,
                spentByRecipient, spentByOccasion);
    }

    public RecipientInsightsDTOOut recipientInsights(Long userId, Long recipientId) {
        Recipient recipient = requireOwnedRecipient(userId, recipientId);

        List<GiftHistory> gifts = giftHistoryRepository
                .findByRecipient_IdAndUser_IdAndWasGiftedTrueOrderByCreatedAtDesc(recipientId, userId);

        double totalSpent = gifts.stream()
                .filter(history -> history.getPriceMinor() != null)
                .mapToDouble(GiftHistory::getPriceMinor)
                .sum();
        long totalGifts = gifts.size();
        double averagePerGift = totalGifts > 0 ? totalSpent / totalGifts : 0.0;

        GiftHistory lastGift = gifts.stream()
                .filter(history -> effectiveDate(history) != null)
                .max(Comparator.comparing(GiftHistoryService::effectiveDate))
                .orElse(null);
        LocalDate lastGiftDate = lastGift != null ? effectiveDate(lastGift) : null;
        String lastGiftName = lastGift != null ? lastGift.getGiftName() : null;

        Map<String, Long> giftsByOccasion = gifts.stream().collect(Collectors.groupingBy(
                GiftHistoryService::occasionOf, Collectors.counting()));
        Map<String, Double> spentByOccasion = gifts.stream().collect(Collectors.groupingBy(
                GiftHistoryService::occasionOf,
                Collectors.summingDouble(history -> history.getPriceMinor() != null ? history.getPriceMinor() : 0.0)));

        List<RecipientInsightsDTOOut.TopStore> topStores = gifts.stream()
                .filter(history -> history.getSelectedProduct() != null)
                .map(GiftHistory::getSelectedProduct)
                .filter(product -> product.getStoreName() != null && !product.getStoreName().isBlank())
                .collect(Collectors.groupingBy(SelectedProduct::getStoreName, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> new RecipientInsightsDTOOut.TopStore(entry.getKey(), entry.getValue()))
                .toList();

        List<RecipientInsightsDTOOut.TimelinePoint> spendTimeline = gifts.stream()
                .filter(history -> effectiveDate(history) != null)
                .sorted(Comparator.comparing(GiftHistoryService::effectiveDate))
                .map(history -> new RecipientInsightsDTOOut.TimelinePoint(
                        effectiveDate(history), history.getGiftName(), history.getPriceMinor()))
                .toList();

        return new RecipientInsightsDTOOut(
                recipient.getName(),
                totalGifts,
                totalSpent,
                averagePerGift,
                lastGiftDate,
                lastGiftName,
                giftsByOccasion,
                spentByOccasion,
                topStores,
                spendTimeline
        );
    }

    private static String occasionOf(GiftHistory history) {
        return history.getOccasionType() != null && !history.getOccasionType().isBlank()
                ? history.getOccasionType() : "UNKNOWN";
    }

    private static boolean withinWindow(LocalDate date, LocalDate from, LocalDate to) {
        if (date == null) {
            return false;
        }
        if (from != null && date.isBefore(from)) {
            return false;
        }
        return to == null || !date.isAfter(to);
    }

    private static LocalDate effectiveDate(GiftHistory history) {
        if (history.getGiftDate() != null) {
            return history.getGiftDate();
        }
        return history.getCreatedAt() != null ? history.getCreatedAt().toLocalDate() : null;
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
        if (!GiftPlanStatus.COMPLETED.equals(giftPlan.getStatus())) {
            giftPlan.setStatus(GiftPlanStatus.COMPLETED);
            giftPlan.setUpdatedAt(LocalDateTime.now());
            giftPlanRepository.save(giftPlan);
        }
    }
}
