package com.edutrack.studentportal.repository;

import com.edutrack.studentportal.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PaymentRepository
        extends MongoRepository<Payment, String> {

    // Get all payments made by a student
    List<Payment> findByStudentId(String studentId);

    List<Payment> findByStudentIdOrderByCreatedAtDesc(String studentId);

    List<Payment> findAllByStudentIdAndCourseId(String studentId, String courseId);

    List<Payment> findByStudentIdAndCourseIdAndStatusIgnoreCase(
            String studentId, String courseId, String status);

    List<Payment> findByCourseId(String courseId);

        List<Payment> findByCourseIdAndStatusIgnoreCase(String courseId, String status);

    // Check whether a payment already exists for an order
    boolean existsByOrderId(String orderId);

    // Find payment using gateway payment ID
    Payment findByPaymentId(String paymentId);

    // Find payment using Razorpay order ID
    Payment findByOrderId(String orderId);
}