package org.example.tahadaw.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SurprisePlanDTOOut {

    private Long id;
    private String planTitle;
    private String steps;
    private String requiredItems;
    private String timingSuggestion;
    private String backupPlan;
    private String aiExplanation;
}
