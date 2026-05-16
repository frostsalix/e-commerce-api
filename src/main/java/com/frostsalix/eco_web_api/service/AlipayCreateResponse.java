package com.frostsalix.eco_web_api.service;

// mock 模式返回拼接 URL，生产模式返回 Alipay SDK pageExecute 生成的 HTML 表单
public record AlipayCreateResponse(
        Long paymentId,
        String outTradeNo,
        String payForm
) {
}
