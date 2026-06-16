package org.example.tahadaw.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.tahadaw.Model.enums.QuestionType;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequiredQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "text not null")
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20) not null")
    private QuestionType questionType;

    @Column(columnDefinition = "boolean not null")
    private Boolean isActive;

    @Column(columnDefinition = "int not null")
    private Integer displayOrder;

    @Column(updatable = false, columnDefinition = "datetime not null")
    private LocalDateTime createdAt;

    @Column(columnDefinition = "datetime not null")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "requiredQuestion", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<RequiredQuestionAnswer> answers;
}
