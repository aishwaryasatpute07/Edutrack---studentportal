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

    @Value("${razorpay.webhook.secret:}")
    private String webhookSecret;

    @Bean
    public RazorpayProperties razorpayProperties() {
        return new RazorpayProperties(keyId, keySecret, webhookSecret);
    }

    public static class RazorpayProperties {

        private final String keyId;
        private final String keySecret;
        private final String webhookSecret;

        public RazorpayProperties(String keyId, String keySecret, String webhookSecret) {
            if (keyId == null || keyId.isBlank()) {
                throw new IllegalStateException(
                        "Required configuration 'razorpay.key.id' (RAZORPAY_KEY_ID) is missing");
            }
            if (keySecret == null || keySecret.isBlank()) {
                throw new IllegalStateException(
                        "Required configuration 'razorpay.key.secret' (RAZORPAY_KEY_SECRET) is missing");
            }
            this.keyId = keyId;
            this.keySecret = keySecret;
            this.webhookSecret = webhookSecret;
        }

        public String getKeyId() {
            return keyId;
        }

        public String getKeySecret() {
            return keySecret;
        }

        public String getWebhookSecret() {
            return webhookSecret;
        }
    }
}