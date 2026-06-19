package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.DTO.IN.ProductSelectDTOIn;
import org.example.tahadaw.DTO.OUT.ProductSearchResultDTOOut;
import org.example.tahadaw.DTO.OUT.SelectedProductDTOOut;
import org.example.tahadaw.Model.GiftIdeaRecommendation;
import org.example.tahadaw.Model.GiftPlan;
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

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${searchapi.api.key:}")
    private String apiKey;

    @Value("${searchapi.base-url:https://www.searchapi.io/api/v1/search}")
    private String baseUrl;

    @Value("${searchapi.gl:sa}")
    private String countryCode;

    @Value("${searchapi.hl:en}")
    private String languageCode;
//    public List<ProductSearchResultDTOOut> searchProducts(Long giftPlanId) {
//        GiftPlan giftPlan = giftPlanRepository.findGiftPlanById(giftPlanId)
//                .orElseThrow(() -> new ApiException("Gift plan not found."));
//
//        GiftIdeaRecommendation selectedIdea = giftIdeaRecommendationRepository
//                .findByGiftPlanAndIsSelectedTrue(giftPlan)
//                .orElseThrow(() -> new ApiException("Select one AI gift idea before searching products."));
//
//        if (selectedIdea.getSearchKeyword() == null || selectedIdea.getSearchKeyword().isBlank()) {
//            throw new ApiException("Selected gift idea has no search keyword.");
//        }
//
//        if (apiKey == null || apiKey.isBlank()) {
//            throw new ApiException(
//                    "SearchAPI.io is not configured. Add searchapi.api.key to application-local.properties.");
//        }
//
//        URI requestUri = buildSearchUri(selectedIdea.getSearchKeyword(), giftPlan.getBudgetMinor(), giftPlan.getCurrency());
//
//        try {
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(requestUri)
//                    .GET()
//                    .build();
//
//            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//            if (response.statusCode() < 200 || response.statusCode() >= 300) {
//                throw new ApiException("SearchAPI.io error: HTTP " + response.statusCode() + " — " + response.body());
//            }
//
//            JsonNode root = JSON.readTree(response.body());
//            JsonNode shoppingResults = root.path("shopping_results");
//            if (!shoppingResults.isArray()) {
//                return List.of();
//            }
//
//            List<ProductSearchResultDTOOut> results = new ArrayList<>();
//            for (JsonNode item : shoppingResults) {
//                ProductSearchResultDTOOut mapped = mapShoppingResult(item, giftPlan.getCurrency());
//                if (mapped != null) {
//                    results.add(mapped);
//                }
//            }
//            return results;
//        } catch (ApiException ex) {
//            throw ex;
//        } catch (InterruptedException ex) {
//            Thread.currentThread().interrupt();
//            throw new ApiException("Product search interrupted.");
//        } catch (Exception ex) {
//            throw new ApiException("Product search failed: " + ex.getMessage());
//        }
//    }

//    @Transactional
//    public SelectedProductDTOOut selectProduct(Long giftPlanId, ProductSelectDTOIn request) {
//        GiftPlan giftPlan = giftPlanRepository.findGiftPlanById(giftPlanId)
//                .orElseThrow(() -> new ApiException("Gift plan not found."));
//
//        GiftIdeaRecommendation selectedIdea = giftIdeaRecommendationRepository
//                .findByGiftPlanAndIsSelectedTrue(giftPlan)
//                .orElseThrow(() -> new ApiException("Select one AI gift idea before selecting a product."));
//
//        if (selectedIdea.getSelectedProducts().isEmpty()) {
//            throw new ApiException("A product is already selected for this gift plan.");
//        }
//        if (selectedIdea.getSelectedProducts().size() > 1) {
//            throw new ApiException("You can only select 1 product for this gift plan.");
//        }
//
//        SelectedProduct selectedProduct = new SelectedProduct();
//        selectedProduct.setGiftIdeaRecommendation(selectedIdea);
//        selectedProduct.setProductName(request.getTitle());
//        selectedProduct.setPrice(request.getPriceMinor());
//        selectedProduct.setCurrency(request.getCurrency() != null ? request.getCurrency() : giftPlan.getCurrency());
//        selectedProduct.setImageUrl(request.getImageUrl());
//        selectedProduct.setProductUrl(request.getProductUrl());
//        selectedProduct.setStoreName(request.getSourceName());
//        selectedProduct.setRating(request.getRating());
//        selectedProduct.setCreatedAt(LocalDateTime.now());
//        selectedProduct = selectedProductRepository.save(selectedProduct);
//
//        giftPlan.setStatus("PRODUCT_SELECTED");
//        giftPlan.setUpdatedAt(LocalDateTime.now());
//        giftPlanRepository.save(giftPlan);
//
//        return toSelectedProductDto(selectedProduct);
//    }

    @Transactional
    public void selectProduct(Long userId, Long productId) {
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
        List<SelectedProduct> selectedIdea = selectedProductRepository
                .findSelectedProductByGiftIdeaRecommendationAndIsSelectedTrue(recommendation);


        if (!selectedIdea.isEmpty()) {
            throw new ApiException("You already selected a product for this gift plan.");
        }

        List<SelectedProduct> productIdeas = selectedProductRepository
                .findSelectedProductByGiftIdeaRecommendation(recommendation);

        if (productIdeas.isEmpty()) {
            throw new ApiException("You have to generate a product recommendation before selecting a product.");
        }


        giftPlan.setSelectedProduct(selectedProduct);
        selectedProduct.setIsSelected(true);
        selectedProduct.setGiftPlan(giftPlan);
        selectedProduct.setUser(giftPlan.getUser());
        selectedProduct.setRecipient(giftPlan.getRecipient());
        selectedProductRepository.save(selectedProduct);
        selectedProductRepository.deleteSelectedProductsByGiftIdeaRecommendation(recommendation);
        if (giftPlan.getStatus() == "GIFT_IDEA_SELECTED") {
            giftPlan.setStatus("PRODUCT_SELECTED");
        }
        giftPlan.setUpdatedAt(LocalDateTime.now());
        giftPlanRepository.save(giftPlan);
    }



    public SelectedProductDTOOut getSelectedProduct(Long giftPlanId) {
        GiftPlan giftPlan = giftPlanRepository.findGiftPlanById(giftPlanId)
                .orElseThrow(() -> new ApiException("Gift plan not found."));

        SelectedProduct selectedProduct = selectedProductRepository
                .findSelectedProductByGiftPlan(giftPlan);

        if (selectedProduct == null) {
            throw new ApiException("No product selected yet for this gift plan.");
        }

        return toSelectedProductDto(selectedProduct);
    }

    private URI buildSearchUri(String query, Long budgetMinor, String currency) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("engine", "google_shopping")
                .queryParam("q", query)
                .queryParam("api_key", apiKey)
                .queryParam("gl", "sa")
                .queryParam("hl", "ar");

        if (budgetMinor != null && budgetMinor > 0) {
            builder.queryParam("price_max", budgetMinor / 100.0);
        }

        return builder.build(true).toUri();
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

        return new ProductSearchResultDTOOut(
                title,
                priceMinor,
                defaultCurrency,
                priceLabel,
                imageUrl,
                productUrl,
                sourceName,
                rating
        );
    }

    private SelectedProductDTOOut toSelectedProductDto(SelectedProduct selectedProduct) {
        return new SelectedProductDTOOut(
                selectedProduct.getId(),
                selectedProduct.getProductName(),
                selectedProduct.getPrice(),
                selectedProduct.getCurrency(),
                selectedProduct.getImageUrl(),
                selectedProduct.getProductUrl(),
                selectedProduct.getStoreName(),
                selectedProduct.getRating(),
                selectedProduct.getCreatedAt()
        );
    }
}
