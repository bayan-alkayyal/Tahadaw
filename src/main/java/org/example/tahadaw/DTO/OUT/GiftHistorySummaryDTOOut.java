package org.example.tahadaw.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class GiftHistorySummaryDTOOut {

    private long totalGifts;
    private double totalSpentMinor;
    private Map<String, Long> countByOccasion;
    private Map<String, Long> countByRecipient;
}
