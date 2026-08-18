package com.edutrack.studentportal.controller;

import com.edutrack.studentportal.dto.PaymentDTO;
import com.edutrack.studentportal.service.PaymentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }


    // Create Razorpay order
    @PostMapping("/create-order")
    public PaymentDTO createPaymentOrder(
            @RequestBody PaymentDTO paymentDTO)
            throws Exception {

        return paymentService.createPaymentOrder(paymentDTO);
    }


    // Verify Razorpay payment
    @PostMapping("/verify")
    public PaymentDTO verifyPayment(
            @RequestBody PaymentDTO paymentDTO)
            throws Exception {

        return paymentService.verifyPayment(paymentDTO);
    }
}