package com.frostsalix.eco_web_api.controller;

import com.frostsalix.eco_web_api.common.ApiResponse;
import com.frostsalix.eco_web_api.model.*;
import com.frostsalix.eco_web_api.service.AlipayCreateResponse;
import com.frostsalix.eco_web_api.service.AlipayService;
import com.frostsalix.eco_web_api.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payment")
@Tag(name = "支付", description = "支付单创建、支付确认、支付宝集成")
public class PaymentController {

    private final PaymentService paymentService;
    private final AlipayService alipayService;

    public PaymentController(
            PaymentService paymentService,
            AlipayService alipayService
    ) {
        this.paymentService = paymentService;
        this.alipayService = alipayService;
    }

    @PostMapping("/{orderId}")
    @Operation(summary = "创建支付单")
    public ApiResponse<?> pay(@PathVariable Long orderId) {
        Payment payment = paymentService.createPayment(orderId);
        return ApiResponse.success(payment);
    }

    @PutMapping("/{id}/success")
    @Operation(summary = "确认支付成功")
    public ApiResponse<?> success(@PathVariable Long id) {
        paymentService.success(id);
        return ApiResponse.success("支付成功");
    }

    @PostMapping("/{orderId}/alipay")
    @Operation(summary = "创建支付宝支付订单")
    public ApiResponse<AlipayCreateResponse> createAlipayOrder(
            @PathVariable Long orderId
    ) {
        return ApiResponse.success(alipayService.createAlipayOrder(orderId));
    }

    @PostMapping("/webhook")
    @Operation(summary = "支付宝异步通知回调")
    public ApiResponse<String> webhook(
            @RequestParam Map<String, String> params
    ) {
        alipayService.handleWebhook(params);
        return ApiResponse.success("回调处理成功");
    }

    @GetMapping("/query")
    @Operation(summary = "按 outTradeNo 查询支付状态")
    public ApiResponse<Payment> queryPayment(
            @RequestParam String outTradeNo
    ) {
        return ApiResponse.success(alipayService.queryOrder(outTradeNo));
    }
}