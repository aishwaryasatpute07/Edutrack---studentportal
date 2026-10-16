package com.edutrack.studentportal.dto;

import jakarta.validation.constraints.DecimalMin;

public class EnrollmentFeeUpdateDTO {

    @DecimalMin(value = "0.0", message = "Sports fee cannot be negative")
    private double sportsFee;

    @DecimalMin(value = "0.0", message = "Examination fee cannot be negative")
    private double examinationFee;

    @DecimalMin(value = "0.0", message = "Other charges cannot be negative")
    private double otherCharges;

    @DecimalMin(value = "0.0", message = "Discount cannot be negative")
    private double discount;

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
}
