package com.frostsalix.eco_web_api.service;

public record AlipayCreateResponse(
        Long paymentId,
        String outTradeNo,
        String payUrl
) {
}
