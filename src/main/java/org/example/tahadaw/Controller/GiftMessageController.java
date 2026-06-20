package org.example.tahadaw.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tahadaw.DTO.IN.GiftMessageCreateDTOIn;
import org.example.tahadaw.DTO.IN.GiftMessageGenerateDTOIn;
import org.example.tahadaw.DTO.IN.GiftMessageUpdateDTOIn;
import org.example.tahadaw.DTO.OUT.GiftMessageDTOOut;
import org.example.tahadaw.Service.GiftMessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gift-messages")
@RequiredArgsConstructor
public class GiftMessageController {

    private final GiftMessageService giftMessageService;

    // AI writes the message from the context the user supplies (no gift plan).
    @PostMapping("/generate")
    public ResponseEntity<GiftMessageDTOOut> generate(@RequestParam Long userId,
                                                      @Valid @RequestBody GiftMessageGenerateDTOIn request) {
        return ResponseEntity.ok(giftMessageService.generate(userId, request));
    }

    // User writes their own message. Body carries only the text.
    @PostMapping("/manual")
    public ResponseEntity<GiftMessageDTOOut> createManual(@RequestParam Long userId,
                                                          @Valid @RequestBody GiftMessageCreateDTOIn request) {
        return ResponseEntity.ok(giftMessageService.createManual(userId, request));
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<GiftMessageDTOOut> update(@RequestParam Long userId,
                                                    @PathVariable Long messageId,
                                                    @Valid @RequestBody GiftMessageUpdateDTOIn request) {
        return ResponseEntity.ok(giftMessageService.update(userId, messageId, request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<GiftMessageDTOOut>> listMine(@RequestParam Long userId) {
        return ResponseEntity.ok(giftMessageService.listMine(userId));
    }

    @GetMapping("/{messageId}")
    public ResponseEntity<GiftMessageDTOOut> getOne(@RequestParam Long userId,
                                                    @PathVariable Long messageId) {
        return ResponseEntity.ok(giftMessageService.getOne(userId, messageId));
    }
}
