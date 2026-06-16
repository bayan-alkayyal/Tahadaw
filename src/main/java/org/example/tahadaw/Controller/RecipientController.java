package org.example.tahadaw.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiResponse;
import org.example.tahadaw.DTO.OUT.GiftHistoryDTOOut;
import org.example.tahadaw.Model.Recipient;
import org.example.tahadaw.Service.GiftHistoryService;
import org.example.tahadaw.Service.RecipientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recipients")
@RequiredArgsConstructor
public class RecipientController {

    private final RecipientService recipientService;
    private final GiftHistoryService giftHistoryService;

    @PostMapping("/add/{userId}")
    public ResponseEntity<?> addRecipient(@PathVariable Long userId, @RequestBody @Valid Recipient recipient) {
        recipientService.addRecipient(userId, recipient);
        return ResponseEntity.status(200).body(new ApiResponse("Recipient added successfully"));
    }

    @GetMapping("/get")
    public ResponseEntity<?> getRecipients() {
        return ResponseEntity.status(200).body(recipientService.getRecipients());
    }

    @PutMapping("/update/{recipientId}")
    public ResponseEntity<?> updateRecipient(@PathVariable Long recipientId, @RequestBody @Valid Recipient recipient) {
        recipientService.updateRecipient(recipientId, recipient);
        return ResponseEntity.status(200).body(new ApiResponse("Recipient updated successfully"));
    }

    @DeleteMapping("/delete/{recipientId}")
    public ResponseEntity<?> deleteRecipient(@PathVariable Long recipientId) {
        recipientService.deleteRecipient(recipientId);
        return ResponseEntity.status(200).body(new ApiResponse("Recipient deleted successfully"));
    }

    @GetMapping("/{recipientId}/gift-history")
    public ResponseEntity<List<GiftHistoryDTOOut>> listGiftHistory(@RequestParam Long userId,
                                                                    @PathVariable Long recipientId) {
        return ResponseEntity.ok(giftHistoryService.listByRecipient(userId, recipientId));
    }
}
