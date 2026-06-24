package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.AI.AiJsonParser;
import org.example.tahadaw.AI.AiService;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.DTO.IN.GroupGiftCreateDTOIn;
import org.example.tahadaw.DTO.IN.GroupGiftUpdateDTOIn;
import org.example.tahadaw.DTO.OUT.*;
import org.example.tahadaw.Mapper.ResponseMapper;
import org.example.tahadaw.Model.*;
import org.example.tahadaw.Repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.webmvc.autoconfigure.WebMvcProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;


import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GroupGiftService {

    private static final Logger log = LoggerFactory.getLogger(GroupGiftService.class);

    private final GroupGiftRepository groupGiftRepository;
    private final UserRepository userRepository;
    private final RecipientRepository recipientRepository;
    private final GroupGiftOptionRepository groupGiftOptionRepository;
    private final AiService aiService;
    private final GroupGiftInviteRepository groupGiftInviteRepository;
    private final GroupGiftVoteRepository groupGiftVoteRepository;
    private final EmailService emailService;

    @Transactional
    public GroupGiftDTOOut create(Long userId, GroupGiftCreateDTOIn request) {
        User owner = userRepository.findUserById(userId)
                .orElseThrow(() -> new ApiException("User not found."));
        Recipient recipient = requireOwnedRecipient(userId, request.getRecipientId());

        GroupGift groupGift = new GroupGift();
        groupGift.setOwner(owner);
        groupGift.setRecipient(recipient);
        groupGift.setTitle(request.getTitle());
        groupGift.setDescription(request.getDescription());
        groupGift.setResponsiblePersonName(owner.getFullName());
        groupGift.setResponsiblePersonEmail(owner.getEmail());
        groupGift.setGiftGivingDate(request.getGiftGivingDate());
        groupGift.setVotingDeadline(request.getVotingDeadline());
        groupGift.setStatus("OPEN");
        groupGift.setCreatedAt(LocalDateTime.now());

        return ResponseMapper.toGroupGiftDto(groupGiftRepository.save(groupGift));
    }

    public List<GroupGiftDTOOut> listMine(Long userId) {
        userRepository.findUserById(userId)
                .orElseThrow(() -> new ApiException("User not found."));

        return groupGiftRepository.findByOwner_IdOrderByCreatedAtDesc(userId).stream()
                .map(ResponseMapper::toGroupGiftDto)
                .toList();
    }

    public GroupGiftDTOOut getOne(Long userId, Long groupGiftId) {
        return ResponseMapper.toGroupGiftDto(requireOwnedGroupGift(userId, groupGiftId));
    }

    @Transactional
    public GroupGiftDTOOut update(Long userId, Long groupGiftId, GroupGiftUpdateDTOIn request) {
        GroupGift groupGift = requireOwnedGroupGift(userId, groupGiftId);

        if (request.getTitle() != null) {
            groupGift.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            groupGift.setDescription(request.getDescription());
        }
        if (request.getGiftGivingDate() != null) {
            groupGift.setGiftGivingDate(request.getGiftGivingDate());
        }
        if (request.getVotingDeadline() != null) {
            groupGift.setVotingDeadline(request.getVotingDeadline());
        }
        // status is intentionally NOT editable here; it is owned by the voting
        // state machine (OPEN on create -> CLOSED via closeVoting).

        return ResponseMapper.toGroupGiftDto(groupGiftRepository.save(groupGift));
    }

    @Transactional
    public void delete(Long userId, Long groupGiftId) {
        GroupGift groupGift = requireOwnedGroupGift(userId, groupGiftId);
        groupGiftRepository.delete(groupGift);
    }

    private Recipient requireOwnedRecipient(Long userId, Long recipientId) {
        Recipient recipient = recipientRepository.findRecipientById(recipientId)
                .orElseThrow(() -> new ApiException("Recipient not found."));
        if (!recipient.getUser().getId().equals(userId)) {
            throw new ApiException("Recipient not found.");
        }
        return recipient;
    }

    private GroupGift requireOwnedGroupGift(Long userId, Long groupGiftId) {
        GroupGift groupGift = groupGiftRepository.findGroupGiftById(groupGiftId)
                .orElseThrow(() -> new ApiException("Group gift not found."));
        if (!groupGift.getOwner().getId().equals(userId)) {
            throw new ApiException("Group gift not found.");
        }
        return groupGift;
    }

    //Bayan
    public void addGroupGiftOption(Long userId, Long groupGiftId, GroupGiftOption groupGiftOption) {

        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new ApiException("User not found"));

        GroupGift groupGift = groupGiftRepository.findGroupGiftById(groupGiftId)
                .orElseThrow(() -> new ApiException("Group gift not found"));

        if (!groupGift.getOwner().getId().equals(user.getId())) {
            throw new ApiException("This group gift does not belong to this user");
        }

        ensureVotingOpen(groupGift);

        groupGiftOption.setGroupGift(groupGift);
        groupGiftOption.setCreatedAt(LocalDateTime.now());

        groupGiftOptionRepository.save(groupGiftOption);
    }


    //Bayan
    @Transactional
    public void generateAiOptions(Long userId, Long groupGiftId) {

        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new ApiException("User not found"));

        GroupGift groupGift = groupGiftRepository.findGroupGiftById(groupGiftId)
                .orElseThrow(() -> new ApiException("Group gift not found"));

        if (!groupGift.getOwner().getId().equals(user.getId())) {
            throw new ApiException("This group gift does not belong to this user");
        }

        ensureVotingOpen(groupGift);

        Recipient recipient = groupGift.getRecipient();

        if (recipient == null) {
            throw new ApiException("Group gift recipient not found");
        }

        List<GroupGiftOption> existingOptions =
                groupGiftOptionRepository.findAllByGroupGift_Id(groupGiftId);
        for (GroupGiftOption existing : existingOptions) {
            if (groupGiftVoteRepository.countByGroupGiftOption_Id(existing.getId()) > 0) {
                throw new ApiException("Cannot regenerate options after votes have been cast.");
            }
        }
        if (!existingOptions.isEmpty()) {
            groupGiftOptionRepository.deleteAll(existingOptions);
            groupGiftOptionRepository.flush();
        }

        String prompt = buildGroupGiftOptionsPrompt(groupGift, recipient);

        String aiResponse = aiService.ask(prompt);

        JsonNode root = AiJsonParser.parseObject(aiResponse);

        JsonNode options = root.get("options");

        if (options == null || !options.isArray()) {
            throw new ApiException("AI response must contain options array");
        }

        for (JsonNode optionNode : options) {

            GroupGiftOption option = new GroupGiftOption();

            option.setGroupGift(groupGift);

            option.setGiftName(AiJsonParser.requireText(optionNode, "giftName"));
            option.setDescription(AiJsonParser.requireText(optionNode, "description"));
            option.setPriceBand(AiJsonParser.requireText(optionNode, "priceBand"));
            option.setReason(AiJsonParser.requireText(optionNode, "reason"));

            option.setCreatedAt(LocalDateTime.now());

            groupGiftOptionRepository.save(option);
        }
    }

    //Bayan
    private String buildGroupGiftOptionsPrompt(GroupGift groupGift, Recipient recipient) {

        return """
            You are an AI assistant that suggests gift options for a group gift voting feature.
            
            The user is creating a group gift poll.
            Generate exactly 3 suitable gift options for the recipient.
            
            Return JSON only in this exact format.
            
            Important:
            - Return one JSON object only.
            - Keep the JSON keys exactly in English.
            - Write all values in Arabic.
            - Generate exactly 3 gift options.
            - Make the options different from each other.
            - Each option should be realistic and suitable for the recipient.
            - priceBand should be written in Saudi Riyal.
            - priceBand must use Arabic-Indic digits only.
            - priceBand format must be exactly like this: "٢٠٠ - ٣٠٠ ريال".
            - Do not use English numbers in priceBand like 200 or 300.
            - Do not add any text before or after the JSON.
            - Do not use markdown.
            
            {
              "options": [
                {
                  "giftName": "اسم الهدية بالعربي",
                  "description": "وصف مختصر للهدية بالعربي",
                  "priceBand": "مثال: ٢٠٠ - ٣٠٠ ريال",
                  "reason": "سبب ترشيح هذه الهدية بالعربي"
                },
                {
                  "giftName": "اسم الهدية بالعربي",
                  "description": "وصف مختصر للهدية بالعربي",
                  "priceBand": "مثال: ٣٠٠ - ٤٥٠ ريال",
                  "reason": "سبب ترشيح هذه الهدية بالعربي"
                },
                {
                  "giftName": "اسم الهدية بالعربي",
                  "description": "وصف مختصر للهدية بالعربي",
                  "priceBand": "مثال: ١٥٠ - ٢٥٠ ريال",
                  "reason": "سبب ترشيح هذه الهدية بالعربي"
                }
              ]
            }
            
            Group gift:
            Title: %s
            Description: %s
            Gift giving date: %s
            Voting deadline: %s
            Responsible person name: %s
            Responsible person email: %s
            
            Recipient profile:
            Name: %s
            Relationship: %s
            Age: %s
            Gender: %s
            Interests: %s
            Hobbies: %s
            Favorite colors: %s
            Favorite brands: %s
            Dislikes: %s
            Personality style: %s
            Size info: %s
            Notes: %s
            """.formatted(
                groupGift.getTitle(),
                groupGift.getDescription(),
                groupGift.getGiftGivingDate(),
                groupGift.getVotingDeadline(),
                ownerDisplayName(groupGift),
                ownerEmail(groupGift),
                recipient.getName(),
                recipient.getRelationship(),
                recipient.getAge(),
                recipient.getGender(),
                recipient.getInterests(),
                recipient.getHobbies(),
                recipient.getFavoriteColors(),
                recipient.getFavoriteBrands(),
                recipient.getDislikes(),
                recipient.getPersonalityStyle(),
                recipient.getSizeInfo(),
                recipient.getNotes()
        );
    }

    //Bayan
    public List<GroupGiftOptionDTOOut> getOptions(Long userId, Long groupGiftId) {
        requireOwnedGroupGift(userId, groupGiftId);
        return groupGiftOptionRepository.findAllByGroupGift_Id(groupGiftId).stream()
                .map(ResponseMapper::toGroupGiftOptionDto)
                .toList();
    }

    //Bayan
    @Transactional
    public List<GroupGiftInviteDTOOut> sendInvites(Long userId, Long groupGiftId, List<GroupGiftInvite> invites) {

        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new ApiException("User not found"));

        GroupGift groupGift = groupGiftRepository.findGroupGiftById(groupGiftId)
                .orElseThrow(() -> new ApiException("Group gift not found"));

        if (!groupGift.getOwner().getId().equals(user.getId())) {
            throw new ApiException("This group gift does not belong to this user");
        }

        ensureVotingOpen(groupGift);

        List<GroupGiftInviteDTOOut> result = new ArrayList<>();

        for (GroupGiftInvite currentInvite : invites) {

            String token = UUID.randomUUID().toString().replace("-", "");

            while (groupGiftInviteRepository.findByToken(token).isPresent()) {
                token = UUID.randomUUID().toString().replace("-", "");
            }

            currentInvite.setId(null);
            currentInvite.setGroupGift(groupGift);
            currentInvite.setToken(token);
            currentInvite.setStatus("PENDING");
            currentInvite.setCreatedAt(LocalDateTime.now());

            GroupGiftInvite savedInvite = groupGiftInviteRepository.save(currentInvite);

            // Email delivery must not abort the whole batch: one invitee's failure
            // (bad address, SMTP hiccup) should not block invites to the rest.
            try {
                emailService.sendGroupGiftInviteEmail(savedInvite, groupGift);
            } catch (Exception e) {
                log.warn("Failed to send group-gift invite email to {} (invite {}): {}",
                        savedInvite.getInviteeEmail(), savedInvite.getId(), e.getMessage());
            }

            result.add(ResponseMapper.toGroupGiftInviteDto(savedInvite));
        }

        return result;
    }


    //Bayan
    public GroupGiftVotePageDTOOut getVotePageData(String token) {

        GroupGiftInvite invite = groupGiftInviteRepository.findByToken(token)
                .orElseThrow(() -> new ApiException("Invalid vote token"));

        GroupGift groupGift = invite.getGroupGift();

        ensureVotingOpen(groupGift);

        List<GroupGiftOption> options = groupGiftOptionRepository.findAllByGroupGift_Id(groupGift.getId());

        return ResponseMapper.toGroupGiftVotePageDto(groupGift, invite, options);
    }

    //Bayan
    @Transactional
    public void submitVote(String token, Long optionId) {

        GroupGiftInvite invite = groupGiftInviteRepository.findByToken(token)
                .orElseThrow(() -> new ApiException("Invalid vote token"));

        GroupGift groupGift = invite.getGroupGift();

        ensureVotingOpen(groupGift);

        if ("VOTED".equals(invite.getStatus())) {
            throw new ApiException("This invite has already voted");
        }

        if (groupGiftVoteRepository.findByInvite_Id(invite.getId()).isPresent()) {
            throw new ApiException("This invite has already voted");
        }

        GroupGiftOption option = groupGiftOptionRepository.findGroupGiftOptionById(optionId)
                .orElseThrow(() -> new ApiException("Group gift option not found"));

        if (!option.getGroupGift().getId().equals(groupGift.getId())) {
            throw new ApiException("This option does not belong to this group gift");
        }

        GroupGiftVote vote = new GroupGiftVote();

        vote.setGroupGift(groupGift);
        vote.setInvite(invite);
        vote.setGroupGiftOption(option);
        vote.setCreatedAt(LocalDateTime.now());

        groupGiftVoteRepository.save(vote);

        invite.setStatus("VOTED");
        groupGiftInviteRepository.save(invite);
    }


    @Transactional
    public void closeVoting(Long userId, Long groupGiftId) {

        GroupGift groupGift = groupGiftRepository.findGroupGiftById(groupGiftId)
                .orElseThrow(() -> new ApiException("Group gift not found"));

        if (!groupGift.getOwner().getId().equals(userId)) {
            throw new ApiException("This group gift does not belong to this user");
        }

        if ("CLOSED".equals(groupGift.getStatus())) {
            throw new ApiException("Voting is already closed");
        }

        List<GroupGiftOption> options = groupGiftOptionRepository.findAllByGroupGift_Id(groupGiftId);

        if (options.isEmpty()) {
            throw new ApiException("No options found for this group gift");
        }

        GroupGiftOption winningOption = null;
        long highestVotes = 0;

        for (GroupGiftOption option : options) {

            long votesCount = groupGiftVoteRepository.countByGroupGiftOption_Id(option.getId());

            if (votesCount > highestVotes) {
                highestVotes = votesCount;
                winningOption = option;
            }
        }

        if (winningOption != null) {
            groupGift.setWinningOption(winningOption);
        } else {
            groupGift.setWinningOption(null);
        }
        groupGift.setStatus("CLOSED");

        groupGiftRepository.save(groupGift);
    }

    private static String ownerDisplayName(GroupGift groupGift) {
        User owner = groupGift.getOwner();
        if (owner != null && owner.getFullName() != null && !owner.getFullName().isBlank()) {
            return owner.getFullName();
        }
        return groupGift.getResponsiblePersonName();
    }

    private static String ownerEmail(GroupGift groupGift) {
        User owner = groupGift.getOwner();
        if (owner != null && owner.getEmail() != null && !owner.getEmail().isBlank()) {
            return owner.getEmail();
        }
        return groupGift.getResponsiblePersonEmail();
    }

    private void ensureVotingOpen(GroupGift groupGift) {
        if ("CLOSED".equals(groupGift.getStatus())) {
            throw new ApiException("Voting is closed");
        }
        if (groupGift.getVotingDeadline() != null
                && LocalDateTime.now().isAfter(groupGift.getVotingDeadline())) {
            throw new ApiException("Voting deadline has passed");
        }
    }


    public GroupGiftResultsDTOOut getResults(Long userId, Long groupGiftId) {

        GroupGift groupGift = groupGiftRepository.findGroupGiftById(groupGiftId)
                .orElseThrow(() -> new ApiException("Group gift not found"));

        if (!groupGift.getOwner().getId().equals(userId)) {
            throw new ApiException("This group gift does not belong to this user");
        }

        List<GroupGiftOption> options = groupGiftOptionRepository.findAllByGroupGift_Id(groupGiftId);

        return ResponseMapper.toGroupGiftResultsDto(
                groupGift,
                options,
                optionId -> groupGiftVoteRepository.countByGroupGiftOption_Id(optionId)
        );
    }





}
