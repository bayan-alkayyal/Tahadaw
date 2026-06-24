package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.AI.AiJsonParser;
import org.example.tahadaw.AI.AiService;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.DTO.OUT.GiftQualityCheckDTOOut;
import org.example.tahadaw.Mapper.ResponseMapper;
import org.example.tahadaw.Model.GiftHistory;
import org.example.tahadaw.Model.GiftQualityCheck;
import org.example.tahadaw.Model.Recipient;
import org.example.tahadaw.Model.User;
import org.example.tahadaw.Repository.GiftHistoryRepository;
import org.example.tahadaw.Repository.GiftQualityCheckRepository;
import org.example.tahadaw.Repository.RecipientRepository;
import org.example.tahadaw.Repository.UserRepository;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GiftQualityCheckService {

    private final GiftQualityCheckRepository giftQualityCheckRepository;
    private final UserRepository userRepository;
    private final RecipientRepository recipientRepository;
    private final GiftHistoryRepository giftHistoryRepository;
    private final AiService aiService;

    //Bayan
    public void runGiftQualityCheck(Long userId , Long recipientId , GiftQualityCheck giftQualityCheck){

        User user = userRepository.findUserById(userId).orElseThrow(() -> new ApiException("User not found"));

        Recipient recipient = recipientRepository.findRecipientByIdAndUser_Id(recipientId,userId)
                .orElseThrow(() -> new ApiException("Recipient not found or does not belong to this user"));


        List<GiftHistory> giftHistories =
                giftHistoryRepository.findByRecipient_IdAndUser_IdOrderByCreatedAtDesc(recipientId, userId);

        String prompt = buildQualityCheckPrompt(recipient, giftQualityCheck, giftHistories);

        String aiResponse = aiService.ask(prompt);

        JsonNode root = AiJsonParser.parseObject(aiResponse);

        giftQualityCheck.setId(null);
        giftQualityCheck.setUser(user);
        giftQualityCheck.setRecipient(recipient);

        giftQualityCheck.setSuitability(AiJsonParser.requireText(root, "suitability"));
        giftQualityCheck.setStrengths(AiJsonParser.requireText(root, "strengths"));
        giftQualityCheck.setWeaknesses(AiJsonParser.requireText(root, "weaknesses"));
        giftQualityCheck.setAiAdvice(AiJsonParser.requireText(root, "aiAdvice"));

        giftQualityCheck.setCreatedAt(LocalDateTime.now());

        giftQualityCheckRepository.save(giftQualityCheck);

    }

    //Bayan
    private String buildQualityCheckPrompt(Recipient recipient,
                                           GiftQualityCheck giftQualityCheck,
                                           List<GiftHistory> giftHistories) {

        StringBuilder historyText = new StringBuilder();

        for (GiftHistory giftHistory : giftHistories) {
            historyText.append("- ")
                    .append(giftHistory.getGiftName())
                    .append("\n");
        }

        if (historyText.isEmpty()) {
            historyText.append("لا يوجد سجل هدايا سابق.");
        }

        return """
            You are an AI assistant that checks if a gift is suitable for a recipient.

            Evaluate the gift based on:
            - Recipient profile
            - Occasion
            - Relationship
            - Gift price / budget
            - Recipient dislikes
            - Previous gift history

            Return JSON only in this exact format.

            Important:
            - Keep the JSON keys exactly in English.
            - The suitability value must be exactly one of these Arabic values only:
              مناسبة, محايد, غير مناسبة
            - Write strengths, weaknesses, and aiAdvice in Arabic.
            - Use a friendly, clear, and helpful Arabic tone for the user.
            - Do not add any text before or after the JSON.
            - Do not use markdown.

            {
              "suitability": "مناسبة or محايد or غير مناسبة",
              "strengths": "اكتب هنا بالعربي لماذا قد تكون الهدية مناسبة",
              "weaknesses": "اكتب هنا بالعربي ما الذي قد يجعل الهدية غير مناسبة",
              "aiAdvice": "اكتب هنا نصيحة واضحة بالعربي للمستخدم"
            }

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

            Gift to check:
            Gift name: %s
            Gift description: %s
            Gift price: %s SAR
            Occasion type: %s

            Previous gift history:
            %s
            """.formatted(
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
                recipient.getNotes(),
                giftQualityCheck.getGiftName(),
                giftQualityCheck.getGiftDescription(),
                giftQualityCheck.getPrice(),
                giftQualityCheck.getOccasionType(),
                historyText
        );
    }


    //Bayan
    public List<GiftQualityCheckDTOOut> getGiftQualityChecksByRecipient(Long userId, Long recipientId) {

        userRepository.findUserById(userId)
                .orElseThrow(() -> new ApiException("User not found"));

        recipientRepository.findRecipientByIdAndUser_Id(recipientId, userId)
                .orElseThrow(() -> new ApiException("Recipient not found or does not belong to this user"));

        return giftQualityCheckRepository.findAllByRecipient_IdAndUser_Id(recipientId, userId).stream()
                .map(ResponseMapper::toGiftQualityCheckDto)
                .toList();
    }


    //Bayan
    public GiftQualityCheckDTOOut getGiftQualityCheckById(Long userId, Long checkId) {

        userRepository.findUserById(userId)
                .orElseThrow(() -> new ApiException("User not found"));

        GiftQualityCheck check = giftQualityCheckRepository.findGiftQualityCheckByIdAndUser_Id(checkId, userId)
                .orElseThrow(() -> new ApiException("Gift quality check not found or does not belong to this user"));
        return ResponseMapper.toGiftQualityCheckDto(check);
    }

}
