package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.Config.ModelMapperConfig;
import org.example.tahadaw.DTO.GoogleShoppingResponse;
import org.example.tahadaw.DTO.ShoppingResult;
import org.example.tahadaw.Model.GiftIdeaRecommendation;
import org.example.tahadaw.Model.GiftPlan;
import org.example.tahadaw.Model.SelectedProduct;
import org.example.tahadaw.Repository.GiftIdeaRecommendationRepository;
import org.example.tahadaw.Repository.GiftPlanRepository;
import org.example.tahadaw.Repository.SelectedProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SearchApiService {

    private final WebClient webClient;
    private  final ModelMapper modelMapper;
    private final GiftPlanRepository giftPlanRepository;
    private final GiftIdeaRecommendationRepository giftIdeaRecommendationRepository;
    private final SelectedProductRepository selectedProductRepository;




    public List<SelectedProduct> search(Long giftPlanId) {
        GiftPlan giftPlan = giftPlanRepository.findGiftPlanById(giftPlanId)
                .orElseThrow(() -> new ApiException("Gift plan not found."));

        GiftIdeaRecommendation selectedIdea = giftIdeaRecommendationRepository
                .findByGiftPlanAndIsSelectedTrue(giftPlan)
                .orElseThrow(() -> new ApiException("Select one AI gift idea before selecting a product."));

        if (selectedIdea == null) {
            throw new ApiException("you havent select product yet");
        }
        //if he select product take product name and search it in google shopping api
        String productName = selectedIdea.getProductName();
        GoogleShoppingResponse response= webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("engine", "google_shopping")
                        .queryParam("q", productName)
                        .queryParam("gl", "sa")
                        .queryParam("hl", "ar")
//                        .queryParam("price_max",giftPlan.getBudgetMinor())
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, resp ->
                        resp.bodyToMono(String.class)
                                .map(body -> new ApiException("Client error: " + body)))
                .onStatus(HttpStatusCode::is5xxServerError, resp ->
                        Mono.error(new ApiException("SearchAPI.io server error")))
                .bodyToMono(GoogleShoppingResponse.class)
                .block();

        if (response == null || response.getShoppingResults() == null) {
            throw new ApiException("No results found for " + productName);
        }

        List<ShoppingResult> dtos = response.getShoppingResults().stream()
                .limit(Math.min(5, response.getShoppingResults().size()))
                .toList();
        List<SelectedProduct> selectedProduct = new ArrayList<>();

        System.out.println(dtos);
        for (ShoppingResult dto : dtos) {
            SelectedProduct selectedProduct1=toDto(dto);
            selectedProduct.add(selectedProduct1);
            selectedProduct1.setGiftIdeaRecommendation(selectedIdea);
           selectedProductRepository.save(selectedProduct1);
        }


        return selectedProduct;

    }


    private SelectedProduct toDto(ShoppingResult r) {
        SelectedProduct selected = new SelectedProduct();
        selected.setProductName(r.getTitle());
        selected.setPrice(r.getExtractedPrice() == null ? null : r.getExtractedPrice() );
        selected.setCurrency("SAR");
        selected.setImageUrl(r.getThumbnail());
        selected.setProductUrl(r.getProductLink());
        selected.setStoreName(r.getSeller());
        selected.setRating(r.getRating());
        selected.setIsSelected(false);
        return selected;
    }
}
