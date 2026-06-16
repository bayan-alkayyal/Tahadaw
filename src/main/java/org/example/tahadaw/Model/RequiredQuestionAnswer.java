package org.example.tahadaw.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_required_answer_plan_question",
        columnNames = {"gift_plan_id", "required_question_id"}
))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequiredQuestionAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "gift_plan_id", nullable = false)
    @JsonIgnore
    private GiftPlan giftPlan;

    @ManyToOne
    @JoinColumn(name = "required_question_id", nullable = false)
    private RequiredQuestion requiredQuestion;

    @Column(columnDefinition = "text not null")
    private String answerText;

    @Column(updatable = false, columnDefinition = "datetime not null")
    private LocalDateTime createdAt;
}
