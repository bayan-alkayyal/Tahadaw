package org.example.tahadaw.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/**
 * Aggregated gift-history figures for a user (dashboard view).
 */
@Data
@AllArgsConstructor
public class GiftHistorySummaryDTOOut {

    private long totalGifts;
    private double totalSpentMinor;
    private long giftedCount;
    private long notGiftedCount;
    private Map<String, Long> countByOccasion;
    private Map<String, Long> countByRecipient;
}
