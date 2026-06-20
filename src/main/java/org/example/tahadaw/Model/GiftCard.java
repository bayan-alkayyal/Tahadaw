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
public class GiftCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gift_message_id", unique = true)
    private GiftMessage giftMessage;

    @Column(columnDefinition = "varchar(100) not null")
    private String recipientName;

    @Column(columnDefinition = "varchar(100) not null")
    private String senderName;

        @Column(columnDefinition = "varchar(10)")
    private String cardSize;

        @Column(columnDefinition = "varchar(20)")
    private String linkType;

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

        @Column(columnDefinition = "varchar(20) not null")
    private String status;

    @Column(updatable = false, columnDefinition = "datetime not null")
    private LocalDateTime createdAt;
}
