package org.example.tahadaw.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SelectedProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "gift_idea_recommendation_id")
    @JsonIgnore
    private GiftIdeaRecommendation giftIdeaRecommendation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    @JsonIgnore
    private Recipient recipient;

    @OneToOne(mappedBy = "selectedProduct", fetch = FetchType.LAZY)
    @JsonIgnore
    private GiftHistory giftHistory;

    @Column(columnDefinition = "varchar(300) not null")
    private String productName;

    @Column(columnDefinition = "double")
    private Double price;

    @Column(columnDefinition = "varchar(255)")
    private String currency;

    @Column(columnDefinition = "varchar(2048)")
    private String imageUrl;

    @Column(columnDefinition = "varchar(2048)")
    private String productUrl;

    @Column(columnDefinition = "varchar(100)")
    private String StoreName;

    @Column(columnDefinition = "double")
    private Double rating;

    @Column(columnDefinition = "boolean not null")
    private Boolean isSelected;

    @Column(updatable = false, columnDefinition = "datetime")
    private LocalDateTime createdAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private GiftPlan giftPlan;
}
