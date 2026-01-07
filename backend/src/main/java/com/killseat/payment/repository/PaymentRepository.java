package com.killseat.payment.repository;

import com.killseat.payment.entity.Payment;
import com.killseat.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByMerchantUid(String merchantUid);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Payment p
           set p.status = :to
         where p.paymentId = :id
           and p.status = :from
    """)
    int updateStatusIfMatch(@Param("id") Long paymentId,
                            @Param("from") PaymentStatus from,
                            @Param("to") PaymentStatus to);
}
