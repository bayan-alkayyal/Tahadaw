package org.example.tahadaw.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Aggregated home-screen payload so the frontend can render the dashboard with a single call
 * instead of hitting reminders, plans, history, premium and group-gift endpoints separately.
 */
@Data
@AllArgsConstructor
public class DashboardDTOOut {

    private long activePlansCount;
    private long pendingGroupGiftVotes;
    private boolean premium;
    private LocalDateTime premiumActivatedAt;
    private List<ReminderItem> upcomingReminders;
    private List<GiftHistoryDTOOut> recentGifts;

    @Data
    @AllArgsConstructor
    public static class ReminderItem {
        private Long id;
        private String message;
        private LocalDateTime reminderDate;
        private String recipientName;
        private String status;
    }
}
