package com.edutrack.studentportal.dto;

import java.util.LinkedHashMap;
import java.util.Map;

public class FeeSummaryDTO {

    private String studentId;

    private String courseId;

    private String courseName;

    private double courseFee;

    private double tuitionFee;

    private Map<String, Double> subjectFees = new LinkedHashMap<>();

    private double sportsFee;

    private double examinationFee;

    private double otherCharges;

    private double discount;

    private double totalFee;

    private double paidAmount;

    private double pendingAmount;

    private String paymentStatus;

    public FeeSummaryDTO() {
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

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public double getCourseFee() {
        return courseFee;
    }

    public void setCourseFee(double courseFee) {
        this.courseFee = courseFee;
    }

    public double getTuitionFee() {
        return tuitionFee;
    }

    public void setTuitionFee(double tuitionFee) {
        this.tuitionFee = tuitionFee;
    }

    public Map<String, Double> getSubjectFees() {
        return subjectFees;
    }

    public void setSubjectFees(Map<String, Double> subjectFees) {
        this.subjectFees = subjectFees == null ? new LinkedHashMap<>() : new LinkedHashMap<>(subjectFees);
    }

    public double getSportsFee() {
        return sportsFee;
    }

    public void setSportsFee(double sportsFee) {
        this.sportsFee = sportsFee;
    }

    public double getExaminationFee() {
        return examinationFee;
    }

    public void setExaminationFee(double examinationFee) {
        this.examinationFee = examinationFee;
    }

    public double getOtherCharges() {
        return otherCharges;
    }

    public void setOtherCharges(double otherCharges) {
        this.otherCharges = otherCharges;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(double totalFee) {
        this.totalFee = totalFee;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public double getPendingAmount() {
        return pendingAmount;
    }

    public void setPendingAmount(double pendingAmount) {
        this.pendingAmount = pendingAmount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}
