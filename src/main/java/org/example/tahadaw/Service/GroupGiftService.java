package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.DTO.IN.GroupGiftCreateDTOIn;
import org.example.tahadaw.DTO.IN.GroupGiftUpdateDTOIn;
import org.example.tahadaw.DTO.OUT.GroupGiftDTOOut;
import org.example.tahadaw.Model.GroupGift;
import org.example.tahadaw.Model.Recipient;
import org.example.tahadaw.Model.User;
import org.example.tahadaw.Model.enums.GroupGiftStatus;
import org.example.tahadaw.Repository.GroupGiftRepository;
import org.example.tahadaw.Repository.RecipientRepository;
import org.example.tahadaw.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupGiftService {

    private final GroupGiftRepository groupGiftRepository;
    private final UserRepository userRepository;
    private final RecipientRepository recipientRepository;

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
        groupGift.setStatus(GroupGiftStatus.OPEN);
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
}
