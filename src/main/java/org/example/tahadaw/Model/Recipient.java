package org.example.tahadaw.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Recipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(columnDefinition = "varchar(100) not null")
    private String name;

    @Column(columnDefinition = "varchar(50)")
    private String relationship;

    @Column(columnDefinition = "int")
    private Integer age;

    @Column(columnDefinition = "varchar(20)")
    private String gender;

    @Column(columnDefinition = "text")
    private String interests;

    @Column(columnDefinition = "text")
    private String hobbies;

    @Column(columnDefinition = "text")
    private String favoriteColors;

    @Column(columnDefinition = "text")
    private String favoriteBrands;

    @Column(columnDefinition = "text")
    private String dislikes;

    @Column(columnDefinition = "varchar(100)")
    private String personalityStyle;

    @Column(columnDefinition = "varchar(100)")
    private String sizeInfo;

    @Column(columnDefinition = "text")
    private String notes;


    @Column(updatable = false, columnDefinition = "datetime not null")
    private LocalDateTime createdAt;

    @Column(columnDefinition = "datetime not null")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<GiftPlan> giftPlans;

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<GiftHistory> giftHistories;

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<SelectedProduct> selectedProducts;

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<GroupGift> groupGifts;

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<GiftQualityCheck> giftQualityChecks;

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Reminder> reminders;
}
