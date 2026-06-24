package org.example.tahadaw.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiResponse;
import org.example.tahadaw.DTO.IN.GroupGiftVoteDTOIn;
import org.example.tahadaw.DTO.OUT.GroupGiftVotePageDTOOut;
import org.example.tahadaw.Service.GroupGiftService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Public (no-auth) endpoints invitees use to view a poll and cast a vote via their token.
 * Kept separate from the owner-facing controller so the public surface lives under /public.
 */
@RestController
@RequestMapping("/api/v1/public/group-gifts")
@RequiredArgsConstructor
public class PublicGroupGiftController {

    private final GroupGiftService groupGiftService;

    @GetMapping("/vote/{token}")
    public ResponseEntity<GroupGiftVotePageDTOOut> getVotePageData(@PathVariable String token) {
        return ResponseEntity.ok(groupGiftService.getVotePageData(token));
    }

    @PostMapping("/vote/{token}")
    public ResponseEntity<?> submitVote(@PathVariable String token,
                                        @RequestBody @Valid GroupGiftVoteDTOIn voteDTOIn) {
        groupGiftService.submitVote(token, voteDTOIn.getGroupGiftOptionId());
        return ResponseEntity.status(200).body(new ApiResponse("Vote submitted successfully"));
    }
}
