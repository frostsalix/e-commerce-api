package com.frostsalix.eco_web_api.service;

import com.frostsalix.eco_web_api.model.Payment;

public interface PaymentService {

    Payment createPayment(Long orderId);
    void success(Long paymentId);

}