package org.example.tahadaw.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiResponse;
import org.example.tahadaw.Model.Reminder;
import org.example.tahadaw.Service.ReminderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @PostMapping("/add/{userId}/{recipientId}")
    public ResponseEntity<?> addReminder(@PathVariable Long userId,
                                         @PathVariable Long recipientId,
                                         @RequestBody @Valid Reminder reminder) {

        reminderService.addReminder(userId, recipientId, reminder);
        return ResponseEntity.status(200).body(new ApiResponse("Reminder added successfully"));
    }

    @GetMapping("/get")
    public ResponseEntity<?> getReminders() {
        return ResponseEntity.status(200).body(reminderService.getReminders());
    }

    @PutMapping("/update/{reminderId}")
    public ResponseEntity<?> updateReminder(@PathVariable Long reminderId, @RequestBody @Valid Reminder reminder) {
        reminderService.updateReminder(reminderId, reminder);
        return ResponseEntity.status(200).body(new ApiResponse("Reminder updated successfully"));
    }


    @DeleteMapping("/delete/{reminderId}")
    public ResponseEntity<?> deleteReminder(@PathVariable Long reminderId) {
        reminderService.deleteReminder(reminderId);
        return ResponseEntity.status(200).body(new ApiResponse("Reminder deleted successfully"));
    }

}
