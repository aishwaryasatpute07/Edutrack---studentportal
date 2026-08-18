package com.edutrack.studentportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class PaymentDTO {

    private String id;

    @NotBlank(message = "student ID is required")
    private String studentId;

    @Positive(message = "amount should be greater than 0")
    private double amount;

    private String orderId;

    private String paymentId;

    private String signature;

    private String paymentMethod;

    private String status;


    // Default Constructor
    public PaymentDTO() {
    }


    // Parameterized Constructor
    public PaymentDTO(String id,
                      String studentId,
                      double amount,
                      String orderId,
                      String paymentId,
                      String signature,
                      String paymentMethod,
                      String status) {

        this.id = id;
        this.studentId = studentId;
        this.amount = amount;
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.signature = signature;
        this.paymentMethod = paymentMethod;
        this.status = status;
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
}