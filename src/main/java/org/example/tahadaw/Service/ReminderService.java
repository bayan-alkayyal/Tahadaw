package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.Model.Recipient;
import org.example.tahadaw.Model.Reminder;
import org.example.tahadaw.Model.User;
import org.example.tahadaw.Model.enums.ReminderStatus;
import org.example.tahadaw.Repository.RecipientRepository;
import org.example.tahadaw.Repository.ReminderRepository;
import org.example.tahadaw.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final RecipientRepository recipientRepository;

    public void addReminder(Long userId, Long recipientId, Reminder reminder) {

        User user = userRepository.findUserById(userId).orElse(null);

        if (user == null) {
            throw new ApiException("User not found");
        }

        Recipient recipient = recipientRepository.findRecipientById(recipientId).orElse(null);

        if (recipient == null) {
            throw new ApiException("Recipient not found");
        }

        if (!recipient.getUser().getId().equals(userId)) {
            throw new ApiException("This recipient does not belong to this user");
        }

        reminder.setUser(user);
        reminder.setRecipient(recipient);
        reminder.setStatus(ReminderStatus.PENDING);
        reminder.setCreatedAt(LocalDateTime.now());

        reminderRepository.save(reminder);
    }

    public List<Reminder> getReminders() {
        return reminderRepository.findAll();
    }

    public void updateReminder(Long reminderId, Reminder reminder) {

        Reminder oldReminder = reminderRepository.findReminderById(reminderId).orElse(null);

        if (oldReminder == null) {
            throw new ApiException("Reminder not found");
        }

        oldReminder.setReminderDate(reminder.getReminderDate());
        oldReminder.setMessage(reminder.getMessage());
        oldReminder.setChannel(reminder.getChannel());

        reminderRepository.save(oldReminder);
    }

    public void deleteReminder(Long reminderId) {

        Reminder reminder = reminderRepository.findReminderById(reminderId).orElse(null);

        if (reminder == null) {
            throw new ApiException("Reminder not found");
        }

        reminderRepository.delete(reminder);
    }

}
