package com.frostsalix.eco_web_api.service;

import com.frostsalix.eco_web_api.model.Payment;
import com.frostsalix.eco_web_api.model.PaymentMethod;
import com.frostsalix.eco_web_api.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlipayServiceTest {

    @Mock
    private PaymentService paymentService;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private AlipaySignatureVerifier alipaySignatureVerifier;

    @InjectMocks
    private AlipayService alipayService;

    @Test
    void shouldCreateAlipayOrderPayload() {
        ReflectionTestUtils.setField(alipayService, "gatewayUrl", "https://openapi.alipay.test/pay");

        Payment payment = new Payment();
        payment.setId(7L);
        payment.setAmount(88.0);
        when(paymentService.createPayment(12L)).thenReturn(payment);
        when(paymentRepository.save(payment)).thenReturn(payment);

        AlipayCreateResponse response = alipayService.createAlipayOrder(12L);

        assertThat(response.paymentId()).isEqualTo(7L);
        assertThat(response.payUrl()).contains("https://openapi.alipay.test/pay");
        assertThat(response.outTradeNo()).isNotBlank();
        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        assertThat(captor.getValue().getPaymentMethod()).isEqualTo(PaymentMethod.ALIPAY);
        assertThat(captor.getValue().getOutTradeNo()).isNotBlank();
    }

    @Test
    void shouldConfirmPaymentWhenWebhookIsSuccessful() {
        Payment payment = new Payment();
        payment.setId(9L);
        when(alipaySignatureVerifier.verify(anyMap())).thenReturn(true);
        when(paymentRepository.findByOutTradeNo("OUT-001")).thenReturn(Optional.of(payment));

        alipayService.handleWebhook(Map.of(
                "out_trade_no", "OUT-001",
                "trade_no", "ALI-TRADE-001",
                "trade_status", "TRADE_SUCCESS",
                "sign", "demo-sign"
        ));

        verify(paymentService).success(9L);
    }

    @Test
    void shouldRejectWebhookWhenSignatureVerificationFails() {
        when(alipaySignatureVerifier.verify(anyMap())).thenReturn(false);

        assertThatThrownBy(() -> alipayService.handleWebhook(Map.of(
                "out_trade_no", "OUT-001",
                "trade_no", "ALI-TRADE-001",
                "trade_status", "TRADE_SUCCESS",
                "sign", "bad-sign"
        ))).isInstanceOf(RuntimeException.class)
                .hasMessage("支付宝验签失败");
    }
}
