package com.frostsalix.eco_web_api.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AlipaySignatureVerifierImpl implements AlipaySignatureVerifier {

    @Value("${alipay.public-key:}")
    private String alipayPublicKey;

    @Value("${alipay.charset:utf-8}")
    private String charset;

    @Value("${alipay.sign-type:RSA2}")
    private String signType;

    @Value("${alipay.notify-sign:}")
    private String fallbackNotifySign;

    @Override
    public boolean verify(Map<String, String> params) {
        if (alipayPublicKey != null && !alipayPublicKey.isBlank()) {
            try {
                return AlipaySignature.rsaCheckV1(
                        params,
                        alipayPublicKey,
                        charset,
                        signType
                );
            } catch (AlipayApiException e) {
                throw new RuntimeException("支付宝验签异常", e);
            }
        }

        String sign = params.get("sign");
        if (fallbackNotifySign == null || fallbackNotifySign.isBlank()) {
            throw new RuntimeException("缺少支付宝验签配置");
        }

        return sign != null && sign.equals(fallbackNotifySign);
    }
}
