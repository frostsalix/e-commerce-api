package com.frostsalix.eco_web_api.service;

import com.frostsalix.eco_web_api.model.Payment;
import com.frostsalix.eco_web_api.model.PaymentMethod;
import com.frostsalix.eco_web_api.model.PaymentStatus;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

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

    // ── 9.1.1 createAlipayOrder ──

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

    // ── 9.1.2 回调验签 ──

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

    @Test
    void shouldRejectWebhookWhenOutTradeNoMissing() {
        when(alipaySignatureVerifier.verify(anyMap())).thenReturn(true);

        assertThatThrownBy(() -> alipayService.handleWebhook(Map.of(
                "trade_no", "ALI-TRADE-001",
                "trade_status", "TRADE_SUCCESS",
                "sign", "demo-sign"
        ))).isInstanceOf(RuntimeException.class)
                .hasMessage("缺少 out_trade_no");
    }

    @Test
    void shouldRejectWebhookWhenOutTradeNoBlank() {
        when(alipaySignatureVerifier.verify(anyMap())).thenReturn(true);

        assertThatThrownBy(() -> alipayService.handleWebhook(Map.of(
                "out_trade_no", "  ",
                "trade_no", "ALI-TRADE-001",
                "trade_status", "TRADE_SUCCESS",
                "sign", "demo-sign"
        ))).isInstanceOf(RuntimeException.class)
                .hasMessage("缺少 out_trade_no");
    }

    @Test
    void shouldRejectWebhookWhenTradeStatusNotSuccess() {
        when(alipaySignatureVerifier.verify(anyMap())).thenReturn(true);

        assertThatThrownBy(() -> alipayService.handleWebhook(Map.of(
                "out_trade_no", "OUT-001",
                "trade_no", "ALI-TRADE-001",
                "trade_status", "TRADE_CLOSED",
                "sign", "demo-sign"
        ))).isInstanceOf(RuntimeException.class)
                .hasMessage("交易未成功");
    }

    @Test
    void shouldAcceptWebhookWhenTradeFinished() {
        Payment payment = new Payment();
        payment.setId(10L);
        when(alipaySignatureVerifier.verify(anyMap())).thenReturn(true);
        when(paymentRepository.findByOutTradeNo("OUT-001")).thenReturn(Optional.of(payment));

        alipayService.handleWebhook(Map.of(
                "out_trade_no", "OUT-001",
                "trade_no", "ALI-TRADE-002",
                "trade_status", "TRADE_FINISHED",
                "sign", "demo-sign"
        ));

        verify(paymentService).success(10L);
    }

    // ── 9.1.3 回调幂等 ──

    @Test
    void shouldSkipSuccessWhenPaymentAlreadySuccessful() {
        Payment payment = new Payment();
        payment.setId(9L);
        payment.setStatus(PaymentStatus.SUCCESS);
        when(alipaySignatureVerifier.verify(anyMap())).thenReturn(true);
        when(paymentRepository.findByOutTradeNo("OUT-001")).thenReturn(Optional.of(payment));

        alipayService.handleWebhook(Map.of(
                "out_trade_no", "OUT-001",
                "trade_no", "ALI-TRADE-001",
                "trade_status", "TRADE_SUCCESS",
                "sign", "demo-sign"
        ));

        // 幂等：不重复调用 success
        verify(paymentService, never()).success(anyLong());
    }

    @Test
    void shouldNotDeductStockTwiceOnDuplicateWebhook() {
        Payment payment = new Payment();
        payment.setId(9L);
        when(alipaySignatureVerifier.verify(anyMap())).thenReturn(true);
        when(paymentRepository.findByOutTradeNo("OUT-001")).thenReturn(Optional.of(payment));

        // 第一次回调
        alipayService.handleWebhook(Map.of(
                "out_trade_no", "OUT-001",
                "trade_no", "ALI-TRADE-001",
                "trade_status", "TRADE_SUCCESS",
                "sign", "demo-sign"
        ));
        verify(paymentService).success(9L);

        // payment 已成功
        payment.setStatus(PaymentStatus.SUCCESS);

        // 第二次回调（幂等）
        alipayService.handleWebhook(Map.of(
                "out_trade_no", "OUT-001",
                "trade_no", "ALI-TRADE-001",
                "trade_status", "TRADE_SUCCESS",
                "sign", "demo-sign"
        ));

        // success 总共只调用一次
        verify(paymentService, times(1)).success(9L);
    }

    // ── 9.1.4 主动查单 ──

    @Test
    void shouldQueryOrderByOutTradeNo() {
        Payment payment = new Payment();
        payment.setId(11L);
        payment.setOutTradeNo("OUT-002");
        payment.setStatus(PaymentStatus.SUCCESS);
        when(paymentRepository.findByOutTradeNo("OUT-002")).thenReturn(Optional.of(payment));

        Payment result = alipayService.queryOrder("OUT-002");

        assertThat(result.getId()).isEqualTo(11L);
        assertThat(result.getOutTradeNo()).isEqualTo("OUT-002");
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    void shouldThrowWhenQueryOrderNotFound() {
        when(paymentRepository.findByOutTradeNo("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alipayService.queryOrder("UNKNOWN"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("支付单不存在");
    }

    // ── 签名验证实现 ──

    @Test
    void shouldVerifyWithFallbackWhenPublicKeyNotConfigured() {
        AlipaySignatureVerifierImpl verifier = new AlipaySignatureVerifierImpl();
        ReflectionTestUtils.setField(verifier, "alipayPublicKey", "");
        ReflectionTestUtils.setField(verifier, "fallbackNotifySign", "demo-sign");
        ReflectionTestUtils.setField(verifier, "charset", "utf-8");
        ReflectionTestUtils.setField(verifier, "signType", "RSA2");

        assertThat(verifier.verify(Map.of("sign", "demo-sign"))).isTrue();
        assertThat(verifier.verify(Map.of("sign", "bad-sign"))).isFalse();
    }

    @Test
    void shouldRejectWhenNoSignInParams() {
        AlipaySignatureVerifierImpl verifier = new AlipaySignatureVerifierImpl();
        ReflectionTestUtils.setField(verifier, "alipayPublicKey", "");
        ReflectionTestUtils.setField(verifier, "fallbackNotifySign", "demo-sign");
        ReflectionTestUtils.setField(verifier, "charset", "utf-8");
        ReflectionTestUtils.setField(verifier, "signType", "RSA2");

        assertThat(verifier.verify(Map.of())).isFalse();
    }
}
