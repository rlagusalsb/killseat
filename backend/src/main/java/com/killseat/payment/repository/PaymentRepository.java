package com.killseat.payment.repository;

import com.killseat.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByMerchantUid(String merchantUid);
}
