package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.DTO.IN.ProductSelectDTOIn;
import org.example.tahadaw.DTO.OUT.ProductSearchResultDTOOut;
import org.example.tahadaw.DTO.OUT.SelectedProductDTOOut;
import org.example.tahadaw.Mapper.ResponseMapper;
import org.example.tahadaw.Model.GiftIdeaRecommendation;
import org.example.tahadaw.Model.GiftPlan;
import org.example.tahadaw.Model.GiftPlanStatus;
import org.example.tahadaw.Model.SelectedProduct;
import org.example.tahadaw.Repository.GiftIdeaRecommendationRepository;
import org.example.tahadaw.Repository.GiftPlanRepository;
import org.example.tahadaw.Repository.SelectedProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductSearchService {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final GiftPlanRepository giftPlanRepository;
    private final GiftIdeaRecommendationRepository giftIdeaRecommendationRepository;
    private final SelectedProductRepository selectedProductRepository;


    @Transactional
    public void selectProduct(Long userId ,Long productId) {
        SelectedProduct selectedProduct=selectedProductRepository.findSelectedProductById(productId);
        if(selectedProduct==null){
            throw new ApiException("Product not found");
        }

        GiftIdeaRecommendation recommendation = giftIdeaRecommendationRepository.findGiftIdeaRecommendationById(selectedProduct.getGiftIdeaRecommendation().getId())
                .orElseThrow(() -> new ApiException("Gift idea recommendation not found."));

        GiftPlan giftPlan = recommendation.getGiftPlan();
        if (!giftPlan.getUser().getId().equals(userId)) {
            throw new ApiException("Gift plan is not yours");
        }
        if (giftPlan.getSelectedProduct() != null) {
            throw new ApiException("You already selected a product for this gift plan.");
        }

        GiftIdeaRecommendation planSelectedIdea = giftPlan.getSelectedGiftIdea();
        if (planSelectedIdea == null || !planSelectedIdea.getId().equals(recommendation.getId())) {
            throw new ApiException("Product does not belong to the selected gift idea for this plan.");
        }

        if (!recommendation.getGiftPlan().getId().equals(giftPlan.getId())) {
            throw new ApiException("Product does not belong to this gift plan.");
        }

        giftPlan.setSelectedProduct(selectedProduct);
        selectedProduct.setIsSelected(true);
        if (selectedProduct.getCreatedAt() == null) {
            selectedProduct.setCreatedAt(LocalDateTime.now());
        }
        selectedProduct.setGiftPlan(giftPlan);
        selectedProduct.setUser(giftPlan.getUser());
        selectedProduct.setRecipient(giftPlan.getRecipient());
        selectedProductRepository.save(selectedProduct);
        selectedProductRepository.deleteSelectedProductsByGiftIdeaRecommendation(recommendation);
        if (GiftPlanStatus.GIFT_IDEA_SELECTED.equals(giftPlan.getStatus())) {
            giftPlan.setStatus(GiftPlanStatus.PRODUCT_SELECTED);
        }
        giftPlan.setUpdatedAt(LocalDateTime.now());
        giftPlanRepository.save(giftPlan);
    }



    public SelectedProductDTOOut getSelectedProduct(Long userId, Long giftPlanId) {
        GiftPlan giftPlan = giftPlanRepository.findGiftPlanById(giftPlanId)
                .orElseThrow(() -> new ApiException("Gift plan not found."));
        if (!giftPlan.getUser().getId().equals(userId)) {
            throw new ApiException("Gift plan not found.");
        }

        SelectedProduct selectedProduct = selectedProductRepository
                .findSelectedProductByGiftPlan(giftPlan);

        if (selectedProduct == null) {
            throw new ApiException("No product selected yet for this gift plan.");
        }

        return ResponseMapper.toSelectedProductDto(selectedProduct);
    }

    /**
     * Clears the chosen product for a gift plan so the user can search and pick again.
     * Resets the plan status back to GIFT_IDEA_SELECTED.
     */
    @Transactional
    public void clearSelectedProduct(Long userId, Long giftPlanId) {
        GiftPlan giftPlan = giftPlanRepository.findGiftPlanById(giftPlanId)
                .orElseThrow(() -> new ApiException("Gift plan not found."));
        if (!giftPlan.getUser().getId().equals(userId)) {
            throw new ApiException("Gift plan is not yours");
        }

        SelectedProduct selectedProduct = selectedProductRepository
                .findSelectedProductByGiftPlan(giftPlan);
        if (selectedProduct == null) {
            throw new ApiException("No product selected yet for this gift plan.");
        }

        // break the link on both sides before removing the row
        giftPlan.setSelectedProduct(null);
        selectedProduct.setGiftPlan(null);
        selectedProductRepository.delete(selectedProduct);
        selectedProductRepository.flush();

        if (GiftPlanStatus.PRODUCT_SELECTED.equals(giftPlan.getStatus())) {
            giftPlan.setStatus(GiftPlanStatus.GIFT_IDEA_SELECTED);
        }
        giftPlan.setUpdatedAt(LocalDateTime.now());
        giftPlanRepository.save(giftPlan);
    }

   
    private ProductSearchResultDTOOut mapShoppingResult(JsonNode item, String defaultCurrency) {
        String title = item.path("title").asString(null);
        String productUrl = item.path("link").asString(null);
        if (title == null || title.isBlank() || productUrl == null || productUrl.isBlank()) {
            return null;
        }

        String priceLabel = item.path("price").asString(null);
        Double extractedPrice = item.path("extracted_price").isNumber()
                ? item.path("extracted_price").asDouble()
                : null;
        Long priceMinor = extractedPrice != null ? Math.round(extractedPrice * 100) : null;

        String imageUrl = item.path("thumbnail").asString(null);
        if (imageUrl == null || imageUrl.isBlank()) {
            imageUrl = item.path("image").asString(null);
        }

        String sourceName = item.path("seller").asString(null);
        Double rating = item.path("rating").isNumber() ? item.path("rating").asDouble() : null;

        ProductSearchResultDTOOut dto = new ProductSearchResultDTOOut();
        dto.setTitle(title);
        dto.setCurrency(defaultCurrency);
        dto.setProductUrl(productUrl);
        if (extractedPrice != null) {
            dto.setPrice(extractedPrice);
        }
        if (imageUrl != null && !imageUrl.isBlank()) {
            dto.setImageUrl(imageUrl);
        }
        if (sourceName != null && !sourceName.isBlank()) {
            dto.setSourceName(sourceName);
        }
        if (rating != null) {
            dto.setRating(rating);
        }
        return dto;
    }

}
