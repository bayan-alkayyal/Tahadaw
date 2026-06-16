package org.example.tahadaw.Repository;

import org.example.tahadaw.Model.Payment;
import org.example.tahadaw.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findPaymentById(Long id);

    List<Payment> findByUserOrderByCreatedAtDesc(User user);

    Optional<Payment> findByTransactionId(String transactionId);
}
