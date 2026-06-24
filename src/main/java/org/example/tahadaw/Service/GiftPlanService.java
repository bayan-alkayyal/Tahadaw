package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.DTO.IN.GiftPlanDTOIn;
import org.example.tahadaw.DTO.OUT.GiftPlanDTOOut;
import org.example.tahadaw.DTO.OUT.GiftPlanSummeryDTOOut;
import org.example.tahadaw.DTO.OUT.RecipientDTOOut;
import org.example.tahadaw.DTO.OUT.SelectedProductSummeryDTOOut;
import org.example.tahadaw.Mapper.ResponseMapper;
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

    public List<GiftPlanDTOOut> listByUser(Long userId) {
        userRepository.findUserById(userId)
                .orElseThrow(() -> new ApiException("User not found."));

        return giftPlanRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(ResponseMapper::toGiftPlanDto)
                .toList();
    }

    @Transactional
    public GiftPlanDTOOut createGiftPlan(Long userId, Long recipientId, GiftPlanDTOIn request) {
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
        giftPlan.setStatus(GiftPlanStatus.CREATED);
        giftPlan.setUser(user);
        giftPlan.setRecipient(recipient);
        giftPlan.setCreatedAt(now);
        giftPlan.setUpdatedAt(now);

        return ResponseMapper.toGiftPlanDto(giftPlanRepository.save(giftPlan));
    }

    @Transactional
    public GiftPlanDTOOut updateGiftPlan(Long userId, Long id, GiftPlanDTOIn request) {
        GiftPlan giftPlan = requireOwnedGiftPlan(userId, id);
        giftPlan.setOccasionType(request.getOccasionType());
        giftPlan.setOccasionDate(request.getOccasionDate());
        giftPlan.setBudget(request.getBudget());
        giftPlan.setCurrency(request.getCurrency());
        giftPlan.setPreferredGiftStyle(request.getPreferredGiftStyle());
        giftPlan.setLanguage(request.getLanguage());
        giftPlan.setUpdatedAt(LocalDateTime.now());
        return ResponseMapper.toGiftPlanDto(giftPlanRepository.save(giftPlan));
    }

    @Transactional
    public void deleteGiftPlan(Long userId, Long id) {
        GiftPlan giftPlan = requireOwnedGiftPlan(userId, id);
        giftPlanRepository.delete(giftPlan);
    }

    public List<GiftPlanDTOOut> listAllActiveGiftPlans(Long userId) {
        return giftPlanRepository.findActivePlansByUserId(userId, LocalDate.now()).stream()
                .map(ResponseMapper::toGiftPlanDto)
                .toList();
    }

    public List<GiftPlanDTOOut> listAllPreviousGiftPlans(Long userId) {
        return giftPlanRepository.findPreviousPlansByUserId(userId, LocalDate.now()).stream()
                .map(ResponseMapper::toGiftPlanDto)
                .toList();
    }

    public GiftPlanSummeryDTOOut getGiftPlanSummary(Long userId,Long giftPlanId) {
        GiftPlan giftPlan = giftPlanRepository.findGiftPlanById(giftPlanId)
                .orElseThrow(() -> new ApiException("Gift plan not found."));
        if (!giftPlan.getUser().getId().equals(userId)) {
            throw new ApiException("Gift plan not yours.");
        }

        RecipientDTOOut recipientDto = ResponseMapper.toRecipientSummaryDto(giftPlan.getRecipient());

        SelectedProductSummeryDTOOut selectedProductDto = null;
        SelectedProduct selectedProduct = giftPlan.getSelectedProduct();
        if (selectedProduct != null) {
            selectedProductDto = new SelectedProductSummeryDTOOut(
                    selectedProduct.getProductName(),
                    selectedProduct.getPrice(),
                    selectedProduct.getStoreName()
            );
        }

        GiftPlanSummeryDTOOut summary = new GiftPlanSummeryDTOOut();
        summary.setId(giftPlan.getId());
        summary.setOccasionType(giftPlan.getOccasionType());
        if (giftPlan.getOccasionDate() != null) {
            summary.setOccasionDate(giftPlan.getOccasionDate());
        }
        summary.setRecipient(recipientDto);
        summary.setBudget(giftPlan.getBudget());
        if (selectedProductDto != null) {
            summary.setSelectedProduct(selectedProductDto);
        }
        giftMessageRepository
                .findFirstByGiftPlan_IdOrderByCreatedAtDesc(giftPlan.getId())
                .map(GiftMessage::getMessageText)
                .ifPresent(summary::setMessage);
        return summary;
    }

    public GiftPlanDTOOut getGiftPlanById(Long userId, Long id) {
        return ResponseMapper.toGiftPlanDto(requireOwnedGiftPlan(userId, id));
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
