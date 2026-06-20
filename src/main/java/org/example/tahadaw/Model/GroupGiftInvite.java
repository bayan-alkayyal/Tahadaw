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
public class GroupGiftInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_gift_id", nullable = false)
    @JsonIgnore
    private GroupGift groupGift;

    @Column(columnDefinition = "varchar(100) not null")
    private String inviteeName;

    @Column(columnDefinition = "varchar(100) not null")
    private String inviteeEmail;

    @Column(unique = true, columnDefinition = "varchar(64) not null")
    private String token;

    @Column(columnDefinition = "varchar(20) not null")
    private String status;

    @Column(updatable = false, columnDefinition = "datetime not null")
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "invite", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private GroupGiftVote vote;
}
