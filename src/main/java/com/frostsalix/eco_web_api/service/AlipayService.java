package com.frostsalix.eco_web_api.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.frostsalix.eco_web_api.model.Payment;
import com.frostsalix.eco_web_api.model.PaymentMethod;
import com.frostsalix.eco_web_api.model.PaymentStatus;
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
    private final AlipaySignatureVerifier alipaySignatureVerifier;

    @Value("${alipay.app-id:}")
    private String appId;

    @Value("${alipay.private-key:}")
    private String privateKey;

    @Value("${alipay.public-key:}")
    private String alipayPublicKey;

    @Value("${alipay.gateway-url:https://openapi.alipay.com/gateway.do}")
    private String gatewayUrl;

    @Value("${alipay.charset:utf-8}")
    private String charset;

    @Value("${alipay.sign-type:RSA2}")
    private String signType;

    @Value("${alipay.notify-url:}")
    private String notifyUrl;

    @Value("${alipay.return-url:}")
    private String returnUrl;

    private AlipayClient alipayClient;

    public AlipayService(
            PaymentService paymentService,
            PaymentRepository paymentRepository,
            AlipaySignatureVerifier alipaySignatureVerifier
    ) {
        this.paymentService = paymentService;
        this.paymentRepository = paymentRepository;
        this.alipaySignatureVerifier = alipaySignatureVerifier;
    }

    // 懒加载 AlipayClient，仅在生产凭证齐全时初始化
    private AlipayClient getAlipayClient() {
        if (alipayClient == null && appId != null && !appId.isBlank()
                && privateKey != null && !privateKey.isBlank()) {
            alipayClient = new DefaultAlipayClient(
                    gatewayUrl,
                    appId,
                    privateKey,
                    "json",
                    charset,
                    alipayPublicKey,
                    signType
            );
        }
        return alipayClient;
    }

    // 创建支付宝支付订单，生产环境用 SDK pageExecute，开发环境用 mock URL
    public AlipayCreateResponse createAlipayOrder(Long orderId) {
        Payment payment = paymentService.createPayment(orderId);
        String outTradeNo = "ALI-" + UUID.randomUUID();

        payment.setPaymentMethod(PaymentMethod.ALIPAY);
        payment.setOutTradeNo(outTradeNo);
        paymentRepository.save(payment);

        AlipayClient client = getAlipayClient();
        String payForm;

        if (client != null) {
            payForm = buildRealPayForm(client, outTradeNo, payment);
        } else {
            payForm = buildMockPayUrl(outTradeNo, payment);
        }

        return new AlipayCreateResponse(
                payment.getId(),
                outTradeNo,
                payForm
        );
    }

    // 生产：通过 Alipay SDK pageExecute 生成支付页面 HTML
    private String buildRealPayForm(AlipayClient client, String outTradeNo, Payment payment) {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        if (notifyUrl != null && !notifyUrl.isBlank()) {
            request.setNotifyUrl(notifyUrl);
        }
        if (returnUrl != null && !returnUrl.isBlank()) {
            request.setReturnUrl(returnUrl);
        }

        request.setBizContent(
                "{\"out_trade_no\":\"" + outTradeNo + "\","
                        + "\"total_amount\":\"" + payment.getAmount() + "\","
                        + "\"subject\":\"订单支付\","
                        + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}"
        );

        try {
            AlipayTradePagePayResponse response = client.pageExecute(request);
            if (!response.isSuccess()) {
                throw new RuntimeException("支付宝下单失败: " + response.getMsg()
                        + " (code=" + response.getCode() + ")");
            }
            return response.getBody();
        } catch (AlipayApiException e) {
            throw new RuntimeException("支付宝下单异常", e);
        }
    }

    // 开发回退：手动拼接 mock 支付 URL
    private String buildMockPayUrl(String outTradeNo, Payment payment) {
        return gatewayUrl
                + "?out_trade_no="
                + URLEncoder.encode(outTradeNo, StandardCharsets.UTF_8)
                + "&total_amount="
                + payment.getAmount();
    }

    // 处理支付宝异步通知回调，验签后触发支付成功
    public void handleWebhook(Map<String, String> params) {
        if (!alipaySignatureVerifier.verify(params)) {
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

        // 幂等：已成功的回调直接返回，避免重复扣库存
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return;
        }

        payment.setGatewayTradeNo(params.get("trade_no"));
        paymentRepository.save(payment);

        paymentService.success(payment.getId());
    }

    // 主动查单：按 outTradeNo 查询本地支付状态，用于补偿兜底
    public Payment queryOrder(String outTradeNo) {
        return paymentRepository.findByOutTradeNo(outTradeNo)
                .orElseThrow(() -> new RuntimeException("支付单不存在"));
    }
}
