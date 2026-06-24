package org.example.tahadaw.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class RecipientInsightsDTOOut {

    private String recipientName;
    private long totalGifts;
    private double totalSpent;
    private double averagePerGift;
    private LocalDate lastGiftDate;
    private String lastGiftName;
    private Map<String, Long> giftsByOccasion;
    private Map<String, Double> spentByOccasion;
    private List<TopStore> topStores;
    private List<TimelinePoint> spendTimeline;

    @Data
    @AllArgsConstructor
    public static class TopStore {
        private String storeName;
        private long count;
    }

    @Data
    @AllArgsConstructor
    public static class TimelinePoint {
        private LocalDate date;
        private String giftName;
        private Double price;
    }
}
