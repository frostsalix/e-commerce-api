package com.frostsalix.eco_web_api.controller;

import com.frostsalix.eco_web_api.common.ApiResponse;
import com.frostsalix.eco_web_api.model.*;
import com.frostsalix.eco_web_api.service.PaymentService;
import com.frostsalix.eco_web_api.repository.OrderRepository;
import com.frostsalix.eco_web_api.repository.PaymentRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentController(
            PaymentService paymentService,
            PaymentRepository paymentRepository,
            OrderRepository orderRepository
    ) {
        this.paymentService = paymentService;
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @PostMapping("/{orderId}")
    public ApiResponse<?> pay(@PathVariable Long orderId) {

        Payment payment = paymentService.createPayment(orderId);

        return ApiResponse.success(payment);
    }

    @PutMapping("/{id}/success")
    public ApiResponse<?> success(@PathVariable Long id) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("支付不存在"));

        payment.setStatus(PaymentStatus.valueOf("SUCCESS"));
        payment.setPaidAt(LocalDateTime.now());

        paymentRepository.save(payment);

        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        return ApiResponse.success("支付成功");
    }
}