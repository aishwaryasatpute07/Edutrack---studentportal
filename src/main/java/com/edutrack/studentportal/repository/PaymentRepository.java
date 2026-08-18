package com.edutrack.studentportal.repository;

import com.edutrack.studentportal.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PaymentRepository
        extends MongoRepository<Payment, String> {

    // Get all payments made by a student
    List<Payment> findByStudentId(String studentId);

    // Check whether a payment already exists for an order
    boolean existsByOrderId(String orderId);

    // Find payment using gateway payment ID
    Payment findByPaymentId(String paymentId);

    // Find payment using Razorpay order ID
    Payment findByOrderId(String orderId);
}