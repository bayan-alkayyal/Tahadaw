package org.example.tahadaw.DTO.IN;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Partial manual edit of an existing surprise plan. Only non-null fields are applied.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SurprisePlanUpdateDTOIn {

    private String planTitle;
    private String steps;
    private String requiredItems;
    private String timingSuggestion;
    private String backupPlan;
    private String aiExplanation;
}
