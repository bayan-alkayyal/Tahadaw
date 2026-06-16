package org.example.tahadaw.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.tahadaw.Model.enums.CardSize;
import org.example.tahadaw.Model.enums.GiftCardStatus;
import org.example.tahadaw.Model.enums.LinkType;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GiftCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @OneToOne
    @JoinColumn(name = "gift_plan_id", nullable = false, unique = true)
    @JsonIgnore
    private GiftPlan giftPlan;

    @OneToOne
    @JoinColumn(name = "gift_message_id", unique = true)
    private GiftMessage giftMessage;

    @Column(columnDefinition = "varchar(100) not null")
    private String recipientName;

    @Column(columnDefinition = "varchar(100) not null")
    private String senderName;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(10)")
    private CardSize cardSize;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20)")
    private LinkType linkType;

    @Column(columnDefinition = "varchar(2048)")
    private String linkUrl;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "LONGBLOB")
    @JsonIgnore
    private byte[] qrCodeImage;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(columnDefinition = "LONGBLOB")
    @JsonIgnore
    private byte[] giftCardImage;

    @Column(columnDefinition = "varchar(100)")
    private String sentToEmail;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20) not null")
    private GiftCardStatus status;

    @Column(updatable = false, columnDefinition = "datetime not null")
    private LocalDateTime createdAt;
}
