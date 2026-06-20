package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.AI.AiJsonParser;
import org.example.tahadaw.AI.AiService;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.DTO.IN.GroupGiftCreateDTOIn;
import org.example.tahadaw.DTO.IN.GroupGiftUpdateDTOIn;
import org.example.tahadaw.DTO.OUT.GroupGiftDTOOut;
import org.example.tahadaw.Model.*;
import org.example.tahadaw.Repository.*;
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

    private final GroupGiftRepository groupGiftRepository;
    private final UserRepository userRepository;
    private final RecipientRepository recipientRepository;
    private final GroupGiftOptionRepository groupGiftOptionRepository;
    private final AiService aiService;
    private final GroupGiftInviteRepository groupGiftInviteRepository;
    private final GroupGiftVoteRepository groupGiftVoteRepository;

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
        groupGift.setResponsiblePersonName(request.getResponsiblePersonName());
        groupGift.setResponsiblePersonEmail(request.getResponsiblePersonEmail());
        groupGift.setGiftGivingDate(request.getGiftGivingDate());
        groupGift.setVotingDeadline(request.getVotingDeadline());
        groupGift.setStatus("OPEN");
        groupGift.setCreatedAt(LocalDateTime.now());

        return toDto(groupGiftRepository.save(groupGift));
    }

    public List<GroupGiftDTOOut> listMine(Long userId) {
        userRepository.findUserById(userId)
                .orElseThrow(() -> new ApiException("User not found."));

        return groupGiftRepository.findByOwner_IdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDto)
                .toList();
    }

    public GroupGiftDTOOut getOne(Long userId, Long groupGiftId) {
        return toDto(requireOwnedGroupGift(userId, groupGiftId));
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
        if (request.getResponsiblePersonName() != null) {
            groupGift.setResponsiblePersonName(request.getResponsiblePersonName());
        }
        if (request.getResponsiblePersonEmail() != null) {
            groupGift.setResponsiblePersonEmail(request.getResponsiblePersonEmail());
        }
        if (request.getGiftGivingDate() != null) {
            groupGift.setGiftGivingDate(request.getGiftGivingDate());
        }
        if (request.getVotingDeadline() != null) {
            groupGift.setVotingDeadline(request.getVotingDeadline());
        }
        if (request.getStatus() != null) {
            groupGift.setStatus(request.getStatus());
        }

        return toDto(groupGiftRepository.save(groupGift));
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

    private GroupGiftDTOOut toDto(GroupGift groupGift) {
        return new GroupGiftDTOOut(
                groupGift.getId(),
                groupGift.getOwner().getId(),
                groupGift.getRecipient().getId(),
                groupGift.getTitle(),
                groupGift.getDescription(),
                groupGift.getResponsiblePersonName(),
                groupGift.getResponsiblePersonEmail(),
                groupGift.getGiftGivingDate(),
                groupGift.getVotingDeadline(),
                groupGift.getWinningOption() != null ? groupGift.getWinningOption().getId() : null,
                groupGift.getStatus(),
                groupGift.getCreatedAt()
        );
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

        if (groupGift.getStatus().equals("CLOSED")) {
            throw new ApiException("Cannot add option because voting is closed");
        }

        groupGiftOption.setGroupGift(groupGift);
        groupGiftOption.setCreatedAt(LocalDateTime.now());

        groupGiftOptionRepository.save(groupGiftOption);
    }


    //Bayan
    public void generateAiOptions(Long userId, Long groupGiftId) {

        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new ApiException("User not found"));

        GroupGift groupGift = groupGiftRepository.findGroupGiftById(groupGiftId)
                .orElseThrow(() -> new ApiException("Group gift not found"));

        if (!groupGift.getOwner().getId().equals(user.getId())) {
            throw new ApiException("This group gift does not belong to this user");
        }

        if ("CLOSED".equals(groupGift.getStatus())) {
            throw new ApiException("Cannot generate options because voting is closed");
        }

        Recipient recipient = groupGift.getRecipient();

        if (recipient == null) {
            throw new ApiException("Group gift recipient not found");
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
                - Do not add any text before or after the JSON.
                - Do not use markdown.
                
                {
                  "options": [
                    {
                      "giftName": "اسم الهدية بالعربي",
                      "description": "وصف مختصر للهدية بالعربي",
                      "priceBand": "مثال: 200 - 300 ريال",
                      "reason": "سبب ترشيح هذه الهدية بالعربي"
                    },
                    {
                      "giftName": "اسم الهدية بالعربي",
                      "description": "وصف مختصر للهدية بالعربي",
                      "priceBand": "مثال: 300 - 450 ريال",
                      "reason": "سبب ترشيح هذه الهدية بالعربي"
                    },
                    {
                      "giftName": "اسم الهدية بالعربي",
                      "description": "وصف مختصر للهدية بالعربي",
                      "priceBand": "مثال: 150 - 250 ريال",
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
                groupGift.getResponsiblePersonName(),
                groupGift.getResponsiblePersonEmail(),
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
    public List<GroupGiftOption> getOptions(Long groupGiftId) {

        GroupGift groupGift = groupGiftRepository.findGroupGiftById(groupGiftId)
                .orElseThrow(() -> new ApiException("Group gift not found"));

        return groupGiftOptionRepository.findAllByGroupGift_Id(groupGift.getId());
    }

    //Bayan
    public List<GroupGiftInvite> sendInvites(Long userId, Long groupGiftId, List<GroupGiftInvite> invites) {

        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new ApiException("User not found"));

        GroupGift groupGift = groupGiftRepository.findGroupGiftById(groupGiftId)
                .orElseThrow(() -> new ApiException("Group gift not found"));

        if (!groupGift.getOwner().getId().equals(user.getId())) {
            throw new ApiException("This group gift does not belong to this user");
        }

        if ("CLOSED".equals(groupGift.getStatus())) {
            throw new ApiException("Cannot send invites because voting is closed");
        }

        List<GroupGiftInvite> result = new ArrayList<>();

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
            result.add(savedInvite);
        }

        return result;
    }


    //Bayan
    public Map<String, Object> getVotePageData(String token) {

        GroupGiftInvite invite = groupGiftInviteRepository.findByToken(token)
                .orElseThrow(() -> new ApiException("Invalid vote token"));

        GroupGift groupGift = invite.getGroupGift();

        if ("CLOSED".equals(groupGift.getStatus())) {
            throw new ApiException("Voting is closed");
        }

        List<GroupGiftOption> options = groupGiftOptionRepository.findAllByGroupGift_Id(groupGift.getId());

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("groupGiftId", groupGift.getId());
        response.put("title", groupGift.getTitle());
        response.put("description", groupGift.getDescription());
        response.put("recipientName", groupGift.getRecipient().getName());
        response.put("votingDeadline", groupGift.getVotingDeadline());
        response.put("status", groupGift.getStatus());


        response.put("options", options);

        response.put("inviteeName", invite.getInviteeName());
        response.put("voteStatus", invite.getStatus());

        return response;
    }

    //Bayan
    public void submitVote(String token, Long optionId) {

        GroupGiftInvite invite = groupGiftInviteRepository.findByToken(token)
                .orElseThrow(() -> new ApiException("Invalid vote token"));

        GroupGift groupGift = invite.getGroupGift();

        if (groupGift.getStatus().equals("CLOSED")) {
            throw new ApiException("Voting is closed");
        }

        if (invite.getStatus().equals("VOTED")) {
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

        if (winningOption == null) {
            throw new ApiException("No votes submitted yet");
        }

        groupGift.setWinningOption(winningOption);
        groupGift.setStatus("CLOSED");

        groupGiftRepository.save(groupGift);
    }


    public Map<String, Object> getResults(Long userId, Long groupGiftId) {

        GroupGift groupGift = groupGiftRepository.findGroupGiftById(groupGiftId)
                .orElseThrow(() -> new ApiException("Group gift not found"));

        if (!groupGift.getOwner().getId().equals(userId)) {
            throw new ApiException("This group gift does not belong to this user");
        }

        List<GroupGiftOption> options = groupGiftOptionRepository.findAllByGroupGift_Id(groupGiftId);

        List<Map<String, Object>> results = new ArrayList<>();

        for (GroupGiftOption option : options) {

            long votesCount = groupGiftVoteRepository.countByGroupGiftOption_Id(option.getId());

            Map<String, Object> optionResult = new HashMap<>();

            optionResult.put("optionId", option.getId());
            optionResult.put("giftName", option.getGiftName());
            optionResult.put("votesCount", votesCount);

            results.add(optionResult);
        }

        Map<String, Object> response = new HashMap<>();

        response.put("groupGiftId", groupGift.getId());
        response.put("title", groupGift.getTitle());
        response.put("status", groupGift.getStatus());
        response.put("winningOption", groupGift.getWinningOption() == null ? null : groupGift.getWinningOption().getGiftName());
        response.put("results", results);

        return response;
    }





}
