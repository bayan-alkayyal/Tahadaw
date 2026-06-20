package org.example.tahadaw.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupGift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonIgnore
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private Recipient recipient;

    @Column(columnDefinition = "varchar(200) not null")
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(columnDefinition = "varchar(100)")
    private String responsiblePersonName;

    @Column(columnDefinition = "varchar(100)")
    private String responsiblePersonEmail;

    @Column(columnDefinition = "date")
    private LocalDate giftGivingDate;

    @Column(columnDefinition = "datetime")
    private LocalDateTime votingDeadline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winning_option_id")
    private GroupGiftOption winningOption;

    @Column(columnDefinition = "varchar(20) not null")
    private String status;

    @Column(updatable = false, columnDefinition = "datetime not null")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "groupGift", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<GroupGiftOption> options;

    @OneToMany(mappedBy = "groupGift", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<GroupGiftInvite> invites;

    @OneToMany(mappedBy = "groupGift", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<GroupGiftVote> votes;

    @OneToMany(mappedBy = "groupGift", cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Reminder> reminders;
}
