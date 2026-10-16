package com.edutrack.studentportal.dto;

import jakarta.validation.constraints.NotBlank;

public class CreatePaymentOrderDTO {

    @NotBlank(message = "Course ID is required")
    private String courseId;

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }
}
