package org.example.tahadaw.Mapper;

import org.example.tahadaw.DTO.OUT.*;
import org.example.tahadaw.Model.*;

import java.util.List;

public final class ResponseMapper {

    private ResponseMapper() {
    }

    public static RecipientDTOOut toRecipientDto(Recipient recipient) {
        RecipientDTOOut dto = new RecipientDTOOut();
        dto.setId(recipient.getId());
        dto.setName(recipient.getName());
        setIfPresent(recipient.getRelationship(), dto::setRelationship);
        setIfPresent(recipient.getAge(), dto::setAge);
        setIfPresent(recipient.getGender(), dto::setGender);
        setIfPresent(recipient.getInterests(), dto::setInterests);
        setIfPresent(recipient.getHobbies(), dto::setHobbies);
        setIfPresent(recipient.getFavoriteColors(), dto::setFavoriteColors);
        setIfPresent(recipient.getFavoriteBrands(), dto::setFavoriteBrands);
        setIfPresent(recipient.getDislikes(), dto::setDislikes);
        setIfPresent(recipient.getPersonalityStyle(), dto::setPersonalityStyle);
        setIfPresent(recipient.getSizeInfo(), dto::setSizeInfo);
        setIfPresent(recipient.getNotes(), dto::setNotes);
        return dto;
    }

    public static RecipientDTOOut toRecipientSummaryDto(Recipient recipient) {
        RecipientDTOOut dto = new RecipientDTOOut();
        dto.setName(recipient.getName());
        setIfPresent(recipient.getRelationship(), dto::setRelationship);
        return dto;
    }

    public static GiftPlanDTOOut toGiftPlanDto(GiftPlan giftPlan) {
        GiftPlanDTOOut dto = new GiftPlanDTOOut();
        dto.setId(giftPlan.getId());
        dto.setRecipientId(giftPlan.getRecipient().getId());
        dto.setOccasionType(giftPlan.getOccasionType());
        setIfPresent(giftPlan.getOccasionDate(), dto::setOccasionDate);
        dto.setBudget(giftPlan.getBudget());
        dto.setCurrency(giftPlan.getCurrency());
        setIfPresent(giftPlan.getPreferredGiftStyle(), dto::setPreferredGiftStyle);
        setIfPresent(giftPlan.getLanguage(), dto::setLanguage);
        if (giftPlan.getSelectedGiftIdea() != null) {
            dto.setSelectedGiftIdeaId(giftPlan.getSelectedGiftIdea().getId());
        }
        if (giftPlan.getSelectedProduct() != null) {
            dto.setSelectedProductId(giftPlan.getSelectedProduct().getId());
        }
        return dto;
    }

    public static ReminderDTOOut toReminderDto(Reminder reminder) {
        ReminderDTOOut dto = new ReminderDTOOut();
        dto.setId(reminder.getId());
        if (reminder.getRecipient() != null) {
            dto.setRecipientId(reminder.getRecipient().getId());
        }
        if (reminder.getGiftPlan() != null) {
            dto.setGiftPlanId(reminder.getGiftPlan().getId());
        }
        if (reminder.getGroupGift() != null) {
            dto.setGroupGiftId(reminder.getGroupGift().getId());
        }
        dto.setReminderDate(reminder.getReminderDate());
        setIfPresent(reminder.getMessage(), dto::setMessage);
        return dto;
    }

    public static GiftIdeaRecommendationDTOOut toGiftIdeaRecommendationDto(GiftIdeaRecommendation recommendation) {
        GiftIdeaRecommendationDTOOut dto = new GiftIdeaRecommendationDTOOut();
        dto.setId(recommendation.getId());
        dto.setProductName(recommendation.getProductName());
        setIfPresent(recommendation.getCategory(), dto::setCategory);
        setIfPresent(recommendation.getPriceBand(), dto::setPriceBand);
        setIfPresent(recommendation.getReason(), dto::setReason);
        setIfPresent(recommendation.getEmotionalFit(), dto::setEmotionalFit);
        setIfPresent(recommendation.getPracticalFit(), dto::setPracticalFit);
        setIfPresent(recommendation.getAiExplanation(), dto::setAiExplanation);
        dto.setSelected(Boolean.TRUE.equals(recommendation.getIsSelected()));
        return dto;
    }

    public static ProductSearchResultDTOOut toProductSearchResultDto(SelectedProduct product) {
        ProductSearchResultDTOOut dto = new ProductSearchResultDTOOut();
        dto.setId(product.getId());
        dto.setTitle(product.getProductName());
        dto.setCurrency(product.getCurrency());
        dto.setProductUrl(product.getProductUrl());
        setIfPresent(product.getPrice(), dto::setPrice);
        setIfPresent(product.getImageUrl(), dto::setImageUrl);
        setIfPresent(product.getStoreName(), dto::setSourceName);
        setIfPresent(product.getRating(), dto::setRating);
        return dto;
    }

    public static SelectedProductDTOOut toSelectedProductDto(SelectedProduct product) {
        SelectedProductDTOOut dto = new SelectedProductDTOOut();
        dto.setId(product.getId());
        dto.setTitle(product.getProductName());
        dto.setCurrency(product.getCurrency());
        dto.setProductUrl(product.getProductUrl());
        setIfPresent(product.getPrice(), dto::setPriceMinor);
        setIfPresent(product.getImageUrl(), dto::setImageUrl);
        setIfPresent(product.getStoreName(), dto::setSourceName);
        setIfPresent(product.getRating(), dto::setRating);
        return dto;
    }

    public static GroupGiftOptionDTOOut toGroupGiftOptionDto(GroupGiftOption option) {
        GroupGiftOptionDTOOut dto = new GroupGiftOptionDTOOut();
        dto.setId(option.getId());
        dto.setGiftName(option.getGiftName());
        setIfPresent(option.getDescription(), dto::setDescription);
        setIfPresent(option.getPriceBand(), dto::setPriceBand);
        setIfPresent(option.getReason(), dto::setReason);
        return dto;
    }

    public static GroupGiftInviteDTOOut toGroupGiftInviteDto(GroupGiftInvite invite) {
        GroupGiftInviteDTOOut dto = new GroupGiftInviteDTOOut();
        dto.setId(invite.getId());
        dto.setInviteeName(invite.getInviteeName());
        dto.setInviteeEmail(invite.getInviteeEmail());
        setIfPresent(invite.getToken(), dto::setToken);
        return dto;
    }

    public static GroupGiftVotePageDTOOut toGroupGiftVotePageDto(GroupGift groupGift,
                                                                  GroupGiftInvite invite,
                                                                  List<GroupGiftOption> options) {
        GroupGiftVotePageDTOOut dto = new GroupGiftVotePageDTOOut();
        dto.setTitle(groupGift.getTitle());
        setIfPresent(groupGift.getDescription(), dto::setDescription);
        dto.setRecipientName(groupGift.getRecipient().getName());
        dto.setVotingDeadline(groupGift.getVotingDeadline());
        dto.setVotingOpen(isVotingOpen(groupGift));
        dto.setOptions(options.stream().map(ResponseMapper::toGroupGiftOptionDto).toList());
        dto.setInviteeName(invite.getInviteeName());
        return dto;
    }

    public static GroupGiftResultsDTOOut toGroupGiftResultsDto(GroupGift groupGift,
                                                               List<GroupGiftOption> options,
                                                               java.util.function.Function<Long, Long> voteCounter) {
        GroupGiftResultsDTOOut dto = new GroupGiftResultsDTOOut();
        dto.setTitle(groupGift.getTitle());
        if (groupGift.getWinningOption() != null) {
            dto.setWinningOptionName(groupGift.getWinningOption().getGiftName());
        }
        dto.setResults(options.stream()
                .map(option -> new GroupGiftResultsDTOOut.OptionResult(
                        option.getId(),
                        option.getGiftName(),
                        voteCounter.apply(option.getId())))
                .toList());
        return dto;
    }

    public static GiftQualityCheckDTOOut toGiftQualityCheckDto(GiftQualityCheck check) {
        GiftQualityCheckDTOOut dto = new GiftQualityCheckDTOOut();
        dto.setId(check.getId());
        dto.setGiftName(check.getGiftName());
        setIfPresent(check.getGiftDescription(), dto::setGiftDescription);
        setIfPresent(check.getPrice(), dto::setPrice);
        setIfPresent(check.getOccasionType(), dto::setOccasionType);
        setIfPresent(check.getSuitability(), dto::setSuitability);
        setIfPresent(check.getStrengths(), dto::setStrengths);
        setIfPresent(check.getWeaknesses(), dto::setWeaknesses);
        setIfPresent(check.getAiAdvice(), dto::setAiAdvice);
        return dto;
    }

    public static RequiredQuestionDetailDTOOut toRequiredQuestionDetailDto(RequiredQuestion question) {
        RequiredQuestionDetailDTOOut dto = new RequiredQuestionDetailDTOOut();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestionText());
        dto.setQuestionType(question.getQuestionType());
        dto.setDisplayOrder(question.getDisplayOrder());
        dto.setActive(Boolean.TRUE.equals(question.getIsActive()));
        return dto;
    }

    public static RequiredQuestionAnswerDetailDTOOut toRequiredQuestionAnswerDetailDto(RequiredQuestionAnswer answer) {
        RequiredQuestionAnswerDetailDTOOut dto = new RequiredQuestionAnswerDetailDTOOut();
        dto.setId(answer.getId());
        dto.setRequiredQuestionId(answer.getRequiredQuestion().getId());
        dto.setQuestionText(answer.getRequiredQuestion().getQuestionText());
        dto.setAnswerText(answer.getAnswerText());
        return dto;
    }

    public static AiGeneratedQuestionDTOOut toAiGeneratedQuestionDto(AiGeneratedQuestion question) {
        AiGeneratedQuestionDTOOut dto = new AiGeneratedQuestionDTOOut();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestionText());
        dto.setReasonForQuestion(question.getReasonForQuestion());
        dto.setDisplayOrder(question.getDisplayOrder());
        return dto;
    }

    public static AiQuestionAnswerDetailDTOOut toAiQuestionAnswerDetailDto(AiQuestionAnswer answer) {
        AiQuestionAnswerDetailDTOOut dto = new AiQuestionAnswerDetailDTOOut();
        dto.setId(answer.getId());
        dto.setAiGeneratedQuestionId(answer.getAiGeneratedQuestion().getId());
        dto.setQuestionText(answer.getAiGeneratedQuestion().getQuestionText());
        dto.setAnswerText(answer.getAnswerText());
        return dto;
    }

    public static GiftHistoryDTOOut toGiftHistoryDto(GiftHistory history) {
        GiftHistoryDTOOut dto = new GiftHistoryDTOOut();
        if (history.getSelectedProduct() != null) {
            dto.setId(history.getSelectedProduct().getId());
        } else {
            dto.setId(history.getId());
        }
        if (history.getRecipient() != null) {
            dto.setRecipientId(history.getRecipient().getId());
        }
        dto.setGiftName(history.getGiftName());
        setIfPresent(history.getOccasionType(), dto::setOccasionType);
        setIfPresent(history.getGiftDate(), dto::setGiftDate);
        setIfPresent(history.getPriceMinor(), dto::setPriceMinor);
        if (history.getWasGifted() != null) {
            dto.setWasGifted(history.getWasGifted());
        }
        setIfPresent(history.getUserRating(), dto::setUserRating);
        setIfPresent(history.getNotes(), dto::setNotes);
        return dto;
    }

    public static GiftHistoryDTOOut toGiftHistoryDto(SelectedProduct product) {
        GiftPlan giftPlan = product.getGiftPlan();
        GiftHistory log = product.getGiftHistory();
        GiftHistoryDTOOut dto = new GiftHistoryDTOOut();
        dto.setId(product.getId());
        if (product.getRecipient() != null) {
            dto.setRecipientId(product.getRecipient().getId());
        }
        dto.setGiftName(product.getProductName());
        if (giftPlan != null) {
            setIfPresent(giftPlan.getOccasionType(), dto::setOccasionType);
            setIfPresent(giftPlan.getOccasionDate(), dto::setGiftDate);
        }
        setIfPresent(product.getPrice(), dto::setPriceMinor);
        if (log != null) {
            if (log.getWasGifted() != null) {
                dto.setWasGifted(log.getWasGifted());
            }
            setIfPresent(log.getUserRating(), dto::setUserRating);
            setIfPresent(log.getNotes(), dto::setNotes);
        }
        return dto;
    }

    public static PaymentDTOOut toPaymentDto(Payment payment, String moyasarStatus, String transactionUrl) {
        PaymentDTOOut dto = new PaymentDTOOut();
        dto.setId(payment.getId());
        dto.setAmountMinor(payment.getAmountMinor());
        dto.setCurrency(payment.getCurrency());
        setIfPresent(payment.getStatus(), dto::setStatus);
        setIfPresent(payment.getProvider(), dto::setProvider);
        setIfPresent(payment.getTransactionId(), dto::setTransactionId);
        setIfPresent(moyasarStatus, dto::setMoyasarStatus);
        setIfPresent(transactionUrl, dto::setTransactionUrl);
        setIfPresent(payment.getCreatedAt(), dto::setCreatedAt);
        return dto;
    }

    public static PremiumStatusDTOOut toPremiumStatusDto(boolean premium, java.time.LocalDateTime activatedAt) {
        PremiumStatusDTOOut dto = new PremiumStatusDTOOut();
        dto.setPremium(premium);
        if (premium && activatedAt != null) {
            dto.setActivatedAt(activatedAt);
        }
        return dto;
    }

    public static GroupGiftDTOOut toGroupGiftDto(GroupGift groupGift) {
        GroupGiftDTOOut dto = new GroupGiftDTOOut();
        dto.setId(groupGift.getId());
        dto.setRecipientId(groupGift.getRecipient().getId());
        dto.setTitle(groupGift.getTitle());
        setIfPresent(groupGift.getDescription(), dto::setDescription);
        dto.setGiftGivingDate(groupGift.getGiftGivingDate());
        dto.setVotingDeadline(groupGift.getVotingDeadline());
        dto.setVotingOpen(isVotingOpen(groupGift));
        if (groupGift.getWinningOption() != null) {
            dto.setWinningOptionId(groupGift.getWinningOption().getId());
        }
        return dto;
    }

    private static boolean isVotingOpen(GroupGift groupGift) {
        if (!"OPEN".equals(groupGift.getStatus())) {
            return false;
        }
        return groupGift.getVotingDeadline() == null
                || !java.time.LocalDateTime.now().isAfter(groupGift.getVotingDeadline());
    }

    private static <T> void setIfPresent(T value, java.util.function.Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
