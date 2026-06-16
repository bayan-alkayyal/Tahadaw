package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.DTO.IN.GiftPlanDTOIn;
import org.example.tahadaw.Model.GiftPlan;
import org.example.tahadaw.Model.Recipient;
import org.example.tahadaw.Model.User;
import org.example.tahadaw.Repository.GiftPlanRepository;
import org.example.tahadaw.Repository.RecipientRepository;
import org.example.tahadaw.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GiftPlanService {

    private final GiftPlanRepository giftPlanRepository;
    private final UserRepository userRepository;
    private final RecipientRepository recipientRepository;

    //shahad-CRUD

    public List<GiftPlan> getAllGiftPlan() {
        return giftPlanRepository.findAll();
    }

    public void createGiftPlan(Long userId, Long recipientId, GiftPlanDTOIn request) {
        User user = userRepository.findUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        Recipient recipient = recipientRepository.findRecipientById(recipientId);
        if (recipient == null) {
            throw new IllegalArgumentException("Recipient not found");
        }

        GiftPlan giftPlan = new GiftPlan();
        giftPlan.setOccasionType(request.getOccasionType());
        giftPlan.setOccasionDate(request.getOccasionDate());
        giftPlan.setBudgetMinor(request.getBudgetMinor());
        giftPlan.setCurrency(request.getCurrency());
        giftPlan.setPreferredGiftStyle(request.getPreferredGiftStyle());
        giftPlan.setLanguage(request.getLanguage());
        giftPlan.setUser(user);
        giftPlan.setRecipient(recipient);

        giftPlanRepository.save(giftPlan);
    }

    public void updateGiftPlan(Long id, GiftPlanDTOIn request) {
        GiftPlan oldGiftPlan = getGiftPlanById(id);
        oldGiftPlan.setOccasionType(request.getOccasionType());
        oldGiftPlan.setOccasionDate(request.getOccasionDate());
        oldGiftPlan.setBudgetMinor(request.getBudgetMinor());
        oldGiftPlan.setCurrency(request.getCurrency());
        oldGiftPlan.setPreferredGiftStyle(request.getPreferredGiftStyle());
        oldGiftPlan.setLanguage(request.getLanguage());
        giftPlanRepository.save(oldGiftPlan);
    }

    public void deleteGiftPlan(Long id) {
        GiftPlan giftPlan = getGiftPlanById(id);
        giftPlanRepository.delete(giftPlan);
    }

    public GiftPlan getGiftPlanById(Long id) {
        GiftPlan giftPlan = giftPlanRepository.findGiftPlanById(id);
        if (giftPlan == null) {
            throw new IllegalArgumentException("Gift plan not found");
        }
        return giftPlan;
    }

}
