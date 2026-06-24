package org.example.tahadaw.DTO.OUT;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PaymentDTOOut {

    private Long id;
    private Long amountMinor;
    private String currency;
    private String status;
    private String provider;
    private String transactionId;
    private String moyasarStatus;
    private String transactionUrl;
    private LocalDateTime createdAt;
}
