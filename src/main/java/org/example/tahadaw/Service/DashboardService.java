package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.DTO.OUT.DashboardDTOOut;
import org.example.tahadaw.DTO.OUT.GiftHistoryDTOOut;
import org.example.tahadaw.DTO.OUT.PremiumStatusDTOOut;
import org.example.tahadaw.Repository.GiftPlanRepository;
import org.example.tahadaw.Repository.GroupGiftRepository;
import org.example.tahadaw.Repository.ReminderRepository;
import org.example.tahadaw.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int LIMIT = 5;

    private final UserRepository userRepository;
    private final ReminderRepository reminderRepository;
    private final GiftPlanRepository giftPlanRepository;
    private final GroupGiftRepository groupGiftRepository;
    private final GiftHistoryService giftHistoryService;
    private final PremiumService premiumService;

    @Transactional(readOnly = true)
    public DashboardDTOOut getDashboard(Long userId) {
        userRepository.findUserById(userId)
                .orElseThrow(() -> new ApiException("User not found."));

        LocalDateTime now = LocalDateTime.now();

        List<DashboardDTOOut.ReminderItem> upcomingReminders =
                reminderRepository.findAllByUser_IdOrderByReminderDateAsc(userId).stream()
                        .filter(reminder -> "PENDING".equalsIgnoreCase(reminder.getStatus()))
                        .filter(reminder -> reminder.getReminderDate() != null
                                && !reminder.getReminderDate().isBefore(now))
                        .limit(LIMIT)
                        .map(reminder -> {
                            DashboardDTOOut.ReminderItem item = new DashboardDTOOut.ReminderItem();
                            item.setId(reminder.getId());
                            if (reminder.getMessage() != null) {
                                item.setMessage(reminder.getMessage());
                            }
                            item.setReminderDate(reminder.getReminderDate());
                            if (reminder.getRecipient() != null) {
                                item.setRecipientName(reminder.getRecipient().getName());
                            }
                            return item;
                        })
                        .toList();

        long activePlansCount = giftPlanRepository.countActivePlansByUserId(userId, LocalDate.now());

        long pendingGroupGiftVotes = groupGiftRepository.findByOwner_IdOrderByCreatedAtDesc(userId).stream()
                .filter(groupGift -> "OPEN".equalsIgnoreCase(groupGift.getStatus()))
                .count();

        List<GiftHistoryDTOOut> recentGifts = giftHistoryService.listMine(userId).stream()
                .limit(LIMIT)
                .toList();

        PremiumStatusDTOOut premiumStatus = premiumService.getPremiumStatus(userId);

        DashboardDTOOut dashboard = new DashboardDTOOut();
        dashboard.setActivePlansCount(activePlansCount);
        dashboard.setPendingGroupGiftVotes(pendingGroupGiftVotes);
        dashboard.setPremium(premiumStatus.isPremium());
        if (premiumStatus.getActivatedAt() != null) {
            dashboard.setPremiumActivatedAt(premiumStatus.getActivatedAt());
        }
        dashboard.setUpcomingReminders(upcomingReminders);
        dashboard.setRecentGifts(recentGifts);
        return dashboard;
    }
}
