package com.edutrack.studentportal.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class RazorpayConfigTest {

    @Test
    void rejectsMissingRazorpayCredentials() {
        assertThrows(IllegalStateException.class,
                () -> new RazorpayConfig.RazorpayProperties("", "configured-secret", ""));
        assertThrows(IllegalStateException.class,
                () -> new RazorpayConfig.RazorpayProperties("configured-key", "", ""));
    }
}
