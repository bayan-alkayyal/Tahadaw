package org.example.tahadaw.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiResponse;
import org.example.tahadaw.DTO.IN.GiftCardCreateDTOIn;
import org.example.tahadaw.DTO.IN.GiftCardUpdateDTOIn;
import org.example.tahadaw.DTO.OUT.GiftCardDTOOut;
import org.example.tahadaw.Service.GiftCardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gift-cards")
@RequiredArgsConstructor
public class GiftCardController {

    private final GiftCardService giftCardService;

    @PostMapping
    public ResponseEntity<GiftCardDTOOut> create(@RequestParam Long userId,
                                                 @Valid @RequestBody GiftCardCreateDTOIn request) {
        return ResponseEntity.ok(giftCardService.create(userId, request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<GiftCardDTOOut>> listMine(@RequestParam Long userId) {
        return ResponseEntity.ok(giftCardService.listMine(userId));
    }

    @GetMapping("/{giftCardId}")
    public ResponseEntity<GiftCardDTOOut> getOne(@RequestParam Long userId,
                                                 @PathVariable Long giftCardId) {
        return ResponseEntity.ok(giftCardService.getOne(userId, giftCardId));
    }

    @PutMapping("/{giftCardId}")
    public ResponseEntity<GiftCardDTOOut> update(@RequestParam Long userId,
                                                @PathVariable Long giftCardId,
                                                @Valid @RequestBody GiftCardUpdateDTOIn request) {
        return ResponseEntity.ok(giftCardService.update(userId, giftCardId, request));
    }

    @DeleteMapping("/{giftCardId}")
    public ResponseEntity<ApiResponse> delete(@RequestParam Long userId,
                                              @PathVariable Long giftCardId) {
        giftCardService.delete(userId, giftCardId);
        return ResponseEntity.ok(new ApiResponse("Gift card deleted."));
    }
}
