package org.example.tahadaw.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiQuestionAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "ai_generated_question_id", nullable = false, unique = true)
    @JsonIgnore
    private AiGeneratedQuestion aiGeneratedQuestion;

    @Column(columnDefinition = "text not null")
    private String answerText;

    @Column(updatable = false, columnDefinition = "datetime not null")
    private LocalDate createdAt;
}
