package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.DTO.OUT.ReminderDTOOut;
import org.example.tahadaw.Mapper.ResponseMapper;
import org.example.tahadaw.Model.Recipient;
import org.example.tahadaw.Model.Reminder;
import org.example.tahadaw.Model.User;
import org.example.tahadaw.Repository.RecipientRepository;
import org.example.tahadaw.Repository.ReminderRepository;
import org.example.tahadaw.Repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final RecipientRepository recipientRepository;
    private final WhatsAppService whatsAppService;


    //Bayan CRUD
    public void addReminder(Long userId, Long recipientId, Reminder reminder) {

        User user = userRepository.findUserById(userId).orElseThrow(() -> new ApiException("User not found"));
        Recipient recipient = recipientRepository.findRecipientById(recipientId).orElseThrow(() -> new ApiException("Recipient not found"));

        if (!recipient.getUser().getId().equals(userId)) {
            throw new ApiException("This recipient does not belong to this user");
        }

        reminder.setUser(user);
        reminder.setRecipient(recipient);
        reminder.setStatus("PENDING");
        reminder.setCreatedAt(LocalDateTime.now());

        reminderRepository.save(reminder);
    }

    public List<ReminderDTOOut> getReminders(Long userId) {
        userRepository.findUserById(userId).orElseThrow(() -> new ApiException("User not found"));
        return reminderRepository.findAllByUser_IdOrderByReminderDateAsc(userId).stream()
                .map(ResponseMapper::toReminderDto)
                .toList();
    }

    public void updateReminder(Long userId, Long reminderId, Reminder reminder) {

        Reminder oldReminder = requireOwnedReminder(userId, reminderId);

        oldReminder.setReminderDate(reminder.getReminderDate());
        oldReminder.setMessage(reminder.getMessage());

        reminderRepository.save(oldReminder);
    }

    public void deleteReminder(Long userId, Long reminderId) {

        Reminder reminder = requireOwnedReminder(userId, reminderId);

        reminderRepository.delete(reminder);
    }

    private Reminder requireOwnedReminder(Long userId, Long reminderId) {
        Reminder reminder = reminderRepository.findReminderById(reminderId)
                .orElseThrow(() -> new ApiException("Reminder not found"));
        if (reminder.getUser() == null || !reminder.getUser().getId().equals(userId)) {
            throw new ApiException("Reminder not found");
        }
        return reminder;
    }


    //Bayan
    public List<ReminderDTOOut> getMyReminders(Long userId) {
        return getReminders(userId);
    }

    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Riyadh")
    @Transactional
    public void checkTodayRemindersAndSendWhatsApp() {

        LocalDate today = LocalDate.now();

        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        List<Reminder> reminders = reminderRepository.findTodayPendingReminders("PENDING" , startOfDay , endOfDay);

        for(Reminder reminder : reminders){

            String message = WhatsAppTemplates.buildReminder(reminder.getUser(),reminder);

            whatsAppService.sendWhatsApp(reminder.getUser().getPhoneNumber(),message);

            reminder.setStatus("SENT");
            reminderRepository.save(reminder);
        }

    }

    

}
