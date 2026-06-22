package org.example.tahadaw.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiResponse;
import org.example.tahadaw.DTO.IN.GiftCardCreateDTOIn;
import org.example.tahadaw.DTO.IN.GiftCardSendEmailDTOIn;
import org.example.tahadaw.DTO.IN.GiftCardUpdateDTOIn;
import org.example.tahadaw.DTO.OUT.GiftCardDTOOut;
import org.example.tahadaw.Model.User;
import org.example.tahadaw.Service.GiftCardService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gift-cards")
@RequiredArgsConstructor
public class GiftCardController {

    private final GiftCardService giftCardService;

    @PostMapping
    public ResponseEntity<GiftCardDTOOut> create(@AuthenticationPrincipal User user,
                                                 @Valid @RequestBody GiftCardCreateDTOIn request) {
        return ResponseEntity.ok(giftCardService.create(user.getId(), request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<GiftCardDTOOut>> listMine(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(giftCardService.listMine(user.getId()));
    }

    @GetMapping("/{giftCardId}")
    public ResponseEntity<GiftCardDTOOut> getOne(@AuthenticationPrincipal User user,
                                                 @PathVariable Long giftCardId) {
        return ResponseEntity.ok(giftCardService.getOne(user.getId(), giftCardId));
    }

    @PutMapping("/{giftCardId}")
    public ResponseEntity<GiftCardDTOOut> update(@AuthenticationPrincipal User user,
                                                @PathVariable Long giftCardId,
                                                @Valid @RequestBody GiftCardUpdateDTOIn request) {
        return ResponseEntity.ok(giftCardService.update(user.getId(), giftCardId, request));
    }

    @PostMapping("/{giftCardId}/regenerate")
    public ResponseEntity<GiftCardDTOOut> regenerate(@AuthenticationPrincipal User user,
                                                     @PathVariable Long giftCardId) {
        return ResponseEntity.ok(giftCardService.regenerate(user.getId(), giftCardId));
    }

    @GetMapping(value = "/{giftCardId}/image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> viewImage(@AuthenticationPrincipal User user,
                                            @PathVariable Long giftCardId) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(giftCardService.getCardImage(user.getId(), giftCardId));
    }

    @GetMapping("/{giftCardId}/download")
    public ResponseEntity<byte[]> download(@AuthenticationPrincipal User user,
                                           @PathVariable Long giftCardId,
                                           @RequestParam(defaultValue = "pdf") String format) {
        if ("png".equalsIgnoreCase(format)) {
            byte[] png = giftCardService.getCardImage(user.getId(), giftCardId);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"gift-card-" + giftCardId + ".png\"")
                    .body(png);
        }

        byte[] pdf = giftCardService.getCardPdf(user.getId(), giftCardId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"gift-card-" + giftCardId + ".pdf\"")
                .body(pdf);
    }

    @PostMapping("/{giftCardId}/send-email")
    public ResponseEntity<GiftCardDTOOut> sendEmail(@AuthenticationPrincipal User user,
                                                    @PathVariable Long giftCardId,
                                                    @Valid @RequestBody(required = false) GiftCardSendEmailDTOIn request) {
        String email = request != null ? request.getEmail() : null;
        return ResponseEntity.ok(giftCardService.sendEmail(user.getId(), giftCardId, email));
    }

    @DeleteMapping("/{giftCardId}")
    public ResponseEntity<ApiResponse> delete(@AuthenticationPrincipal User user,
                                              @PathVariable Long giftCardId) {
        giftCardService.delete(user.getId(), giftCardId);
        return ResponseEntity.ok(new ApiResponse("Gift card deleted."));
    }
}
