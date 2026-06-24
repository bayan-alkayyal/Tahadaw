package org.example.tahadaw.Service;

import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiException;
import org.example.tahadaw.DTO.IN.MoyasarCardPaymentDTOIn;
import org.example.tahadaw.DTO.IN.MoyasarWebhookDTOIn;
import org.example.tahadaw.DTO.OUT.PaymentDTOOut;
import org.example.tahadaw.Mapper.ResponseMapper;
import org.example.tahadaw.Model.Payment;
import org.example.tahadaw.Model.User;
import org.example.tahadaw.Repository.PaymentRepository;
import org.example.tahadaw.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Premium one-time payment via Moyasar sandbox (mock/test cards), same approach as Project_3.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String PROVIDER = "MOYASAR";

    private final MoyasarService moyasarService;
    private final PremiumService premiumService;
    private final EmailService emailService;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Value("${premium.amount-minor:9900}")
    private long premiumAmountMinor;

    @Value("${premium.currency:SAR}")
    private String premiumCurrency;

    @Transactional
    public PaymentDTOOut processPremiumPayment(Long userId, MoyasarCardPaymentDTOIn request) {
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new ApiException("User not found."));

        if (Boolean.TRUE.equals(user.getIsPremium())) {
            throw new ApiException("User already has premium access.");
        }

        // Idempotency guard: block a second charge while a premium payment is still pending.
        if (paymentRepository.existsByUser_IdAndPaymentTypeAndStatus(user.getId(), "PREMIUM", "PENDING")) {
            throw new ApiException("A premium payment is already in progress. Complete or wait for it to finish.");
        }

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setAmountMinor(premiumAmountMinor);
        payment.setCurrency(premiumCurrency);
        payment.setPaymentType("PREMIUM");
        payment.setStatus("PENDING");
        payment.setProvider(PROVIDER);
        payment.setCreatedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        JsonNode moyasarResponse = moyasarService.createCardPayment(
                request.getName(),
                request.getNumber(),
                request.getCvc(),
                request.getMonth(),
                request.getYear(),
                premiumAmountMinor,
                premiumCurrency,
                "Tahadaw premium unlock",
                request.getCallbackUrl()
        );

        return syncFromMoyasarResponse(payment, moyasarResponse, user, true);
    }

    @Transactional
    public PaymentDTOOut syncPaymentFromWebhook(MoyasarWebhookDTOIn webhook) {
        Payment payment = paymentRepository.findByTransactionId(webhook.getId())
                .orElseThrow(() -> new ApiException("Payment not found for Moyasar id: " + webhook.getId()));

        JsonNode moyasarResponse = moyasarService.fetchPayment(webhook.getId());
        return syncFromMoyasarResponse(payment, moyasarResponse, payment.getUser(), true);
    }

    @Transactional
    public PaymentDTOOut refreshMoyasarPaymentStatus(String moyasarPaymentId) {
        Payment payment = paymentRepository.findByTransactionId(moyasarPaymentId)
                .orElseThrow(() -> new ApiException("Payment not found for Moyasar id: " + moyasarPaymentId));

        JsonNode moyasarResponse = moyasarService.fetchPayment(moyasarPaymentId);
        return syncFromMoyasarResponse(payment, moyasarResponse, payment.getUser(), true);
    }

    public List<PaymentDTOOut> getMyPayments(Long userId) {
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new ApiException("User not found."));

        return paymentRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(payment -> ResponseMapper.toPaymentDto(payment, null, null))
                .toList();
    }

    private PaymentDTOOut syncFromMoyasarResponse(
            Payment payment, JsonNode moyasarResponse, User user, boolean activatePremiumIfPaid) {
        String moyasarPaymentId = MoyasarService.readPaymentId(moyasarResponse);
        String moyasarStatus = MoyasarService.readStatus(moyasarResponse);
        String transactionUrl = MoyasarService.readTransactionUrl(moyasarResponse);

        if (moyasarPaymentId != null && !moyasarPaymentId.isBlank()) {
            payment.setTransactionId(moyasarPaymentId);
        }

        if ("paid".equalsIgnoreCase(moyasarStatus)) {
            if (activatePremiumIfPaid) {
                premiumService.activatePremium(user, payment);
                try {
                    emailService.sendPaymentReceiptEmail(user, payment);
                } catch (ApiException ignored) {
                    // Email is optional during local development.
                }
            } else {
                payment.setStatus("PAID");
            }
        } else if ("failed".equalsIgnoreCase(moyasarStatus)) {
            payment.setStatus("FAILED");
        } else {
            payment.setStatus("PENDING");
        }

        paymentRepository.save(payment);
        return ResponseMapper.toPaymentDto(payment, moyasarStatus, transactionUrl);
    }
}
