package com.edutrack.studentportal.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Document(collection = "payments")
public class Payment {

    @Id
    private String id;

    private String studentId;

    private String courseId;

    private double amount;

    @Indexed(unique = true, sparse = true)
    private String orderId;

    @Indexed(unique = true, sparse = true)
    private String paymentId;

    private String signature;

    private String paymentMethod;

    private String status;

    private LocalDateTime paymentDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private double totalFeeAtOrder;

    private Map<String, Double> feeBreakdown = new LinkedHashMap<>();

    // Default Constructor
    public Payment() {
    }


    // Parameterized Constructor
    public Payment(String id,
                   String studentId,
                   String courseId,
                   double amount,
                   String orderId,
                   String paymentId,
                   String signature,
                   String paymentMethod,
                   String status,
                   LocalDateTime paymentDate) {

        this.id = id;
        this.studentId = studentId;
        this.courseId = courseId;
        this.amount = amount;
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.signature = signature;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.paymentDate = paymentDate;
    }


    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }


    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }


    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }


    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }


    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }


    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public double getTotalFeeAtOrder() {
        return totalFeeAtOrder;
    }

    public void setTotalFeeAtOrder(double totalFeeAtOrder) {
        this.totalFeeAtOrder = totalFeeAtOrder;
    }

    public Map<String, Double> getFeeBreakdown() {
        return feeBreakdown;
    }

    public void setFeeBreakdown(Map<String, Double> feeBreakdown) {
        this.feeBreakdown = feeBreakdown == null ? new LinkedHashMap<>() : new LinkedHashMap<>(feeBreakdown);
    }
}