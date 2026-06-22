package org.example.tahadaw.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

/**
 * Time-bounded spending breakdown for a user. Extends the simple summary with a date window,
 * an average per gift, and money totals grouped by recipient and by occasion.
 */
@Data
@AllArgsConstructor
public class SpendingStatsDTOOut {

    private LocalDate from;
    private LocalDate to;
    private long giftCount;
    private double totalSpent;
    private double averagePerGift;
    private Map<String, Double> spentByRecipient;
    private Map<String, Double> spentByOccasion;
}
