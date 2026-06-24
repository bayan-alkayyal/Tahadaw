package org.example.tahadaw.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiResponse;
import org.example.tahadaw.DTO.IN.NotificationCreateDTOIn;
import org.example.tahadaw.DTO.IN.NotificationUpdateDTOIn;
import org.example.tahadaw.DTO.OUT.NotificationDTOOut;
import org.example.tahadaw.Model.User;
import org.example.tahadaw.Service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationDTOOut> create(@AuthenticationPrincipal User user,
                                                     @Valid @RequestBody NotificationCreateDTOIn request) {
        return ResponseEntity.ok(notificationService.create(user.getId(), request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<NotificationDTOOut>> listMine(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(notificationService.listMine(user.getId()));
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<NotificationDTOOut> getOne(@AuthenticationPrincipal User user,
                                                       @PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.getOne(user.getId(), notificationId));
    }

    @PutMapping("/{notificationId}")
    public ResponseEntity<NotificationDTOOut> update(@AuthenticationPrincipal User user,
                                                     @PathVariable Long notificationId,
                                                     @Valid @RequestBody NotificationUpdateDTOIn request) {
        return ResponseEntity.ok(notificationService.update(user.getId(), notificationId, request));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationDTOOut> markRead(@AuthenticationPrincipal User user,
                                                       @PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.markRead(user.getId(), notificationId));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse> delete(@AuthenticationPrincipal User user,
                                              @PathVariable Long notificationId) {
        notificationService.delete(user.getId(), notificationId);
        return ResponseEntity.ok(new ApiResponse("Notification deleted."));
    }
}
