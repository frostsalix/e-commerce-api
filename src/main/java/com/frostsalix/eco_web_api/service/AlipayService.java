package com.frostsalix.eco_web_api.service;

import com.frostsalix.eco_web_api.model.Payment;
import com.frostsalix.eco_web_api.model.PaymentMethod;
import com.frostsalix.eco_web_api.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Service
public class AlipayService {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    @Value("${alipay.gateway-url:https://openapi.alipay.com/gateway.do}")
    private String gatewayUrl;

    @Value("${alipay.notify-sign:demo-sign}")
    private String notifySign;

    public AlipayService(
            PaymentService paymentService,
            PaymentRepository paymentRepository
    ) {
        this.paymentService = paymentService;
        this.paymentRepository = paymentRepository;
    }

    public AlipayCreateResponse createAlipayOrder(Long orderId) {
        Payment payment = paymentService.createPayment(orderId);
        String outTradeNo = "ALI-" + UUID.randomUUID();

        payment.setPaymentMethod(PaymentMethod.ALIPAY);
        payment.setOutTradeNo(outTradeNo);
        paymentRepository.save(payment);

        String payUrl = gatewayUrl
                + "?out_trade_no="
                + URLEncoder.encode(outTradeNo, StandardCharsets.UTF_8)
                + "&total_amount="
                + payment.getAmount();

        return new AlipayCreateResponse(
                payment.getId(),
                outTradeNo,
                payUrl
        );
    }

    public void handleWebhook(Map<String, String> params) {
        String sign = params.get("sign");
        if (sign == null || !sign.equals(notifySign)) {
            throw new RuntimeException("支付宝验签失败");
        }

        String outTradeNo = params.get("out_trade_no");
        if (outTradeNo == null || outTradeNo.isBlank()) {
            throw new RuntimeException("缺少 out_trade_no");
        }

        String tradeStatus = params.get("trade_status");
        if (!"TRADE_SUCCESS".equals(tradeStatus)
                && !"TRADE_FINISHED".equals(tradeStatus)) {
            throw new RuntimeException("交易未成功");
        }

        Payment payment = paymentRepository.findByOutTradeNo(outTradeNo)
                .orElseThrow(() -> new RuntimeException("支付单不存在"));

        payment.setGatewayTradeNo(params.get("trade_no"));
        paymentRepository.save(payment);

        paymentService.success(payment.getId());
    }
}
