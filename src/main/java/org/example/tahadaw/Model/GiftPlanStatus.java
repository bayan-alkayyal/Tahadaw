package org.example.tahadaw.Model;

/**
 * Gift plan workflow states. Kept server-side only; not exposed on API DTOs.
 */
public final class GiftPlanStatus {

    public static final String CREATED = "CREATED";
    public static final String REQUIRED_QUESTIONS_ANSWERED = "REQUIRED_QUESTIONS_ANSWERED";
    public static final String AI_QUESTIONS_GENERATED = "AI_QUESTIONS_GENERATED";
    public static final String AI_QUESTIONS_ANSWERED = "AI_QUESTIONS_ANSWERED";
    public static final String RECOMMENDATIONS_GENERATED = "RECOMMENDATIONS_GENERATED";
    public static final String GIFT_IDEA_SELECTED = "GIFT_IDEA_SELECTED";
    public static final String PRODUCT_SELECTED = "PRODUCT_SELECTED";
    public static final String COMPLETED = "COMPLETED";

    /** Legacy typo stored in DB before fix — treated as recommendations generated. */
    public static final String LEGACY_AI_GENERATED_TYPO = "AI_GENERATED_";

    private GiftPlanStatus() {
    }

    public static boolean isAtOrAfterRecommendations(String status) {
        return RECOMMENDATIONS_GENERATED.equals(status)
                || LEGACY_AI_GENERATED_TYPO.equals(status)
                || GIFT_IDEA_SELECTED.equals(status)
                || PRODUCT_SELECTED.equals(status)
                || COMPLETED.equals(status);
    }

    public static boolean canSelectGiftIdea(String status) {
        return RECOMMENDATIONS_GENERATED.equals(status) || LEGACY_AI_GENERATED_TYPO.equals(status);
    }
}
