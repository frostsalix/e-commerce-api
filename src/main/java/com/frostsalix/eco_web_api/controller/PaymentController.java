package com.frostsalix.eco_web_api.controller;

import com.frostsalix.eco_web_api.common.ApiResponse;
import com.frostsalix.eco_web_api.model.*;
import com.frostsalix.eco_web_api.service.PaymentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * 创建支付单（状态为 PENDING，待后续确认支付）
     */
    @PostMapping("/{orderId}")
    public ApiResponse<?> pay(@PathVariable Long orderId) {
        Payment payment = paymentService.createPayment(orderId);
        return ApiResponse.success(payment);
    }

    /**
     * 确认支付成功：扣库存、更新支付状态、更新订单状态
     */
    @PutMapping("/{id}/success")
    public ApiResponse<?> success(@PathVariable Long id) {
        paymentService.success(id);
        return ApiResponse.success("支付成功");
    }
}