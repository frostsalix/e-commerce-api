package com.frostsalix.eco_web_api.controller;

import com.frostsalix.eco_web_api.common.ApiResponse;
import com.frostsalix.eco_web_api.model.Payment;
import com.frostsalix.eco_web_api.service.AlipayService;
import com.frostsalix.eco_web_api.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;
    @Mock
    private AlipayService alipayService;

    @InjectMocks
    private PaymentController paymentController;

    @Test
    void shouldQueryPaymentByOutTradeNo() {
        Payment payment = new Payment();
        payment.setId(3L);
        payment.setOutTradeNo("OUT-100");
        when(alipayService.queryOrder("OUT-100")).thenReturn(payment);

        ApiResponse<Payment> result = paymentController.queryPayment("OUT-100");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getId()).isEqualTo(3L);
        assertThat(result.getData().getOutTradeNo()).isEqualTo("OUT-100");
    }

    @Test
    void shouldReturnPlainSuccessForWebhook() {
        doNothing().when(alipayService).handleWebhook(org.mockito.ArgumentMatchers.anyMap());

        String result = paymentController.webhook(java.util.Map.of(
                "out_trade_no", "OUT-200",
                "trade_no", "ALI-TEST-200",
                "trade_status", "TRADE_SUCCESS",
                "sign", "demo-sign"
        ));

        assertThat(result).isEqualTo("success");
    }
}
