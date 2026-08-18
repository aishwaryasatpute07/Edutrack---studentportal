package com.edutrack.studentportal.service;

import com.edutrack.studentportal.config.RazorpayConfig;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class RazorpayService {

    private final RazorpayConfig.RazorpayProperties razorpayProperties;

    public RazorpayService(RazorpayConfig.RazorpayProperties razorpayProperties) {
        this.razorpayProperties = razorpayProperties;
    }

    public Order createOrder(double amount) throws Exception {

        System.out.println("Razorpay Key ID: " + razorpayProperties.getKeyId());
        System.out.println("Razorpay Secret Loaded: " +
                (razorpayProperties.getKeySecret() != null &&
                        !razorpayProperties.getKeySecret().isBlank()));

        RazorpayClient razorpayClient =
                new RazorpayClient(
                        razorpayProperties.getKeyId(),
                        razorpayProperties.getKeySecret()
                );

        JSONObject orderRequest = new JSONObject();

        // Razorpay expects amount in paise
        int amountInPaise = (int) (amount * 100);

        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "EDUTRACK_" + System.currentTimeMillis());

        return razorpayClient.orders.create(orderRequest);
    }
}