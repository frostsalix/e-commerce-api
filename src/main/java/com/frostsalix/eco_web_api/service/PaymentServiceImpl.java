package com.frostsalix.eco_web_api.service;

import com.frostsalix.eco_web_api.model.*;
import com.frostsalix.eco_web_api.repository.OrderRepository;
import com.frostsalix.eco_web_api.repository.PaymentRepository;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public Payment createPayment(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(order.getTotalPrice());
        payment.setStatus(PaymentStatus.PENDING);

        return paymentRepository.save(payment);
    }
}