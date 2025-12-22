package com.killseat.payment;

import com.killseat.payment.dto.PortOneCancelResult;
import com.killseat.payment.dto.PortOnePaymentInfo;
import com.siot.IamportRestClient.response.Payment;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class PortOneClientImpl implements PortOneClient {

    private final IamportClient iamportClient;

    //단건 조회
    @Override
    public PortOnePaymentInfo getPaymentInfo(String impUid) {
        try {
            IamportResponse<Payment> response =
                    iamportClient.paymentByImpUid(impUid);

            Payment p = response.getResponse();
            if (p == null) {
                return null;
            }

            return new PortOnePaymentInfo(
                    p.getImpUid(),
                    p.getMerchantUid(),
                    p.getStatus(),
                    p.getAmount() == null ? null : p.getAmount().intValue()
            );
        } catch (IamportResponseException | IOException e) {
            //PG 장애/네트워크 오류 → 서비스에서 fail 처리
            return null;
        }
    }

    @Override
    public PortOneCancelResult cancelPayment(String impUid, String reason) {
        try {
            //impUid 기반 전체 취소
            CancelData cancelData = new CancelData(impUid, true);
            cancelData.setReason(reason);

            IamportResponse<Payment> response =
                    iamportClient.cancelPaymentByImpUid(cancelData);

            Payment p = response.getResponse();

            Integer cancelAmount =
                    (p == null || p.getCancelAmount() == null)
                            ? 0
                            : p.getCancelAmount().intValue();

            return new PortOneCancelResult(cancelAmount, response.getMessage());
        } catch (IamportResponseException | IOException e) {
            return new PortOneCancelResult(0, e.getMessage());
        }
    }
}
