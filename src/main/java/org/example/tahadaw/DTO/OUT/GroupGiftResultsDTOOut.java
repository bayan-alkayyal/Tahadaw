package org.example.tahadaw.DTO.OUT;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class GroupGiftResultsDTOOut {

    private String title;
    private String winningOptionName;
    private List<OptionResult> results;

    @Getter
    @AllArgsConstructor
    public static class OptionResult {
        private Long optionId;
        private String giftName;
        private long votesCount;
    }
}
