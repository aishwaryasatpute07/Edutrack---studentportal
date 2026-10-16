package com.edutrack.studentportal.service;

import com.edutrack.studentportal.config.RazorpayConfig;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Payment;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class RazorpayService {

    private final RazorpayConfig.RazorpayProperties razorpayProperties;

    public RazorpayService(RazorpayConfig.RazorpayProperties razorpayProperties) {
        this.razorpayProperties = razorpayProperties;
    }

    private RazorpayClient client() throws Exception {
        return new RazorpayClient(
                razorpayProperties.getKeyId(),
                razorpayProperties.getKeySecret()
        );
    }

    public Order createOrder(long amountInPaise) throws Exception {
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "EDUTRACK_" + System.currentTimeMillis());

        return client().orders.create(orderRequest);
    }

    public Payment fetchPayment(String paymentId) throws Exception {
        return client().payments.fetch(paymentId);
    }

    public String getKeyId() {
        return razorpayProperties.getKeyId();
    }
}