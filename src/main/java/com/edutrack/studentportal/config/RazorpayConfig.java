package com.edutrack.studentportal.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RazorpayConfig {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Bean
    public RazorpayProperties razorpayProperties() {
        return new RazorpayProperties(keyId, keySecret);
    }

    public static class RazorpayProperties {

        private final String keyId;
        private final String keySecret;

        public RazorpayProperties(String keyId, String keySecret) {
            this.keyId = keyId;
            this.keySecret = keySecret;
        }

        public String getKeyId() {
            return keyId;
        }

        public String getKeySecret() {
            return keySecret;
        }
    }
}