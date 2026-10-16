package com.edutrack.studentportal.dto;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class PaymentLedgerDTO {

    private String id;
    private String studentId;
    private String courseId;
    private double amount;
    private String orderId;
    private String paymentId;
    private String paymentMethod;
    private String status;
    private LocalDateTime paymentDate;
    private LocalDateTime createdAt;
    private double totalFeeAtOrder;
    private Map<String, Double> feeBreakdown = new LinkedHashMap<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public double getTotalFeeAtOrder() { return totalFeeAtOrder; }
    public void setTotalFeeAtOrder(double totalFeeAtOrder) { this.totalFeeAtOrder = totalFeeAtOrder; }
    public Map<String, Double> getFeeBreakdown() { return feeBreakdown; }
    public void setFeeBreakdown(Map<String, Double> feeBreakdown) {
        this.feeBreakdown = feeBreakdown == null ? new LinkedHashMap<>() : new LinkedHashMap<>(feeBreakdown);
    }
}