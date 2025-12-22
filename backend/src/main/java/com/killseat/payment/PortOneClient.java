package com.killseat.payment;

import com.killseat.payment.dto.PortOneCancelResult;
import com.killseat.payment.dto.PortOnePaymentInfo;

public interface PortOneClient {
    PortOnePaymentInfo getPaymentInfo(String impUid);
    PortOneCancelResult cancelPayment(String impUid, String reason);
}
