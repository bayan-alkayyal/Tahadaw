package org.example.tahadaw.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiResponse;
import org.example.tahadaw.DTO.OUT.ReminderDTOOut;
import org.example.tahadaw.Model.Reminder;
import org.example.tahadaw.Model.User;
import org.example.tahadaw.Service.ReminderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @PostMapping("/add/{recipientId}")
    public ResponseEntity<?> addReminder(@AuthenticationPrincipal User user,
                                         @PathVariable Long recipientId,
                                         @RequestBody @Valid Reminder reminder) {

        reminderService.addReminder(user.getId(), recipientId, reminder);
        return ResponseEntity.status(200).body(new ApiResponse("Reminder added successfully"));
    }

    @GetMapping("/get")
    public ResponseEntity<List<ReminderDTOOut>> getReminders(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(reminderService.getReminders(user.getId()));
    }

    @PutMapping("/update/{reminderId}")
    public ResponseEntity<?> updateReminder(@AuthenticationPrincipal User user,
                                            @PathVariable Long reminderId,
                                            @RequestBody @Valid Reminder reminder) {
        reminderService.updateReminder(user.getId(), reminderId, reminder);
        return ResponseEntity.status(200).body(new ApiResponse("Reminder updated successfully"));
    }


    @DeleteMapping("/delete/{reminderId}")
    public ResponseEntity<?> deleteReminder(@AuthenticationPrincipal User user, @PathVariable Long reminderId) {
        reminderService.deleteReminder(user.getId(), reminderId);
        return ResponseEntity.status(200).body(new ApiResponse("Reminder deleted successfully"));
    }


    @GetMapping("/get-my")
    public ResponseEntity<List<ReminderDTOOut>> getMyReminders(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(reminderService.getMyReminders(user.getId()));
    }
}
