package org.example.tahadaw.DTO.OUT;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DashboardDTOOut {

    private long activePlansCount;
    private long pendingGroupGiftVotes;
    private boolean premium;
    private LocalDateTime premiumActivatedAt;
    private List<ReminderItem> upcomingReminders;
    private List<GiftHistoryDTOOut> recentGifts;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ReminderItem {
        private Long id;
        private String message;
        private LocalDateTime reminderDate;
        private String recipientName;
    }
}
