package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.DTO.OUT.ProductSearchResultDTOOut;
import org.example.tahadaw.Mapper.ResponseMapper;
import org.example.tahadaw.DTO.GoogleShoppingResponse;
import org.example.tahadaw.DTO.ShoppingResult;
import org.example.tahadaw.Model.GiftIdeaRecommendation;
import org.example.tahadaw.Model.GiftPlan;
import org.example.tahadaw.Model.GiftPlanStatus;
import org.example.tahadaw.Model.SelectedProduct;
import org.example.tahadaw.Repository.GiftIdeaRecommendationRepository;
import org.example.tahadaw.Repository.GiftPlanRepository;
import org.example.tahadaw.Repository.SelectedProductRepository;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchApiService {

    private final WebClient webClient;
    private final GiftPlanRepository giftPlanRepository;
    private final GiftIdeaRecommendationRepository giftIdeaRecommendationRepository;
    private final SelectedProductRepository selectedProductRepository;

    @Transactional
    public List<ProductSearchResultDTOOut> search(Long userId, Long giftPlanId) {
        GiftPlan giftPlan = giftPlanRepository.findGiftPlanById(giftPlanId)
                .orElseThrow(() -> new ApiException("Gift plan not found."));
        if (!giftPlan.getUser().getId().equals(userId)) {
            throw new ApiException("Gift plan not found.");
        }

        GiftIdeaRecommendation selectedIdea = giftIdeaRecommendationRepository
                .findByGiftPlanAndIsSelectedTrue(giftPlan)
                .orElseThrow(() -> new ApiException("Select one AI gift idea before searching for products."));

        if (!GiftPlanStatus.GIFT_IDEA_SELECTED.equals(giftPlan.getStatus())
                && giftPlan.getSelectedGiftIdea() == null) {
            throw new ApiException("Select one AI gift idea before searching for products.");
        }

        if (giftPlan.getSelectedProduct() != null) {
            throw new ApiException("A product is already selected for this gift plan.");
        }

        selectedProductRepository.deleteSelectedProductsByGiftIdeaRecommendation(selectedIdea);

        String productName = selectedIdea.getProductName();
        GoogleShoppingResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("engine", "google_shopping")
                        .queryParam("q", productName)
                        .queryParam("gl", "sa")
                        .queryParam("hl", "ar")
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, resp ->
                        resp.bodyToMono(String.class)
                                .map(body -> new ApiException("Client error: " + body)))
                .onStatus(HttpStatusCode::is5xxServerError, resp ->
                        Mono.error(new ApiException("SearchAPI.io server error")))
                .bodyToMono(GoogleShoppingResponse.class)
                .block();

        if (response == null || response.getShoppingResults() == null || response.getShoppingResults().isEmpty()) {
            throw new ApiException("No results found for " + productName);
        }

        List<ShoppingResult> dtos = response.getShoppingResults().stream()
                .limit(Math.min(5, response.getShoppingResults().size()))
                .toList();
        List<ProductSearchResultDTOOut> results = new ArrayList<>();

        for (ShoppingResult dto : dtos) {
            SelectedProduct selectedProduct = toEntity(dto);
            selectedProduct.setGiftIdeaRecommendation(selectedIdea);
            SelectedProduct saved = selectedProductRepository.save(selectedProduct);
            results.add(ResponseMapper.toProductSearchResultDto(saved));
        }

        return results;
    }

    private SelectedProduct toEntity(ShoppingResult r) {
        SelectedProduct selected = new SelectedProduct();
        selected.setProductName(r.getTitle());
        selected.setPrice(r.getExtractedPrice() == null ? null : r.getExtractedPrice());
        selected.setCurrency("SAR");
        selected.setImageUrl(r.getThumbnail());
        selected.setProductUrl(r.getProductLink());
        selected.setStoreName(r.getSeller());
        selected.setRating(r.getRating());
        selected.setIsSelected(false);
        return selected;
    }
}
