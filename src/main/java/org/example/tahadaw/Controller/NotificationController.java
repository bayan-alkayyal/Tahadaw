package org.example.tahadaw.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiResponse;
import org.example.tahadaw.DTO.IN.NotificationCreateDTOIn;
import org.example.tahadaw.DTO.IN.NotificationUpdateDTOIn;
import org.example.tahadaw.DTO.OUT.NotificationDTOOut;
import org.example.tahadaw.Service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationDTOOut> create(@RequestParam Long userId,
                                                     @Valid @RequestBody NotificationCreateDTOIn request) {
        return ResponseEntity.ok(notificationService.create(userId, request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<NotificationDTOOut>> listMine(@RequestParam Long userId) {
        return ResponseEntity.ok(notificationService.listMine(userId));
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationDTOOut> getOne(@RequestParam Long userId,
                                                       @PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.getOne(userId, notificationId));
    }

    @PutMapping("/{notificationId}")
    public ResponseEntity<NotificationDTOOut> update(@RequestParam Long userId,
                                                     @PathVariable Long notificationId,
                                                     @Valid @RequestBody NotificationUpdateDTOIn request) {
        return ResponseEntity.ok(notificationService.update(userId, notificationId, request));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse> delete(@RequestParam Long userId,
                                              @PathVariable Long notificationId) {
        notificationService.delete(userId, notificationId);
        return ResponseEntity.ok(new ApiResponse("Notification deleted."));
    }
}
