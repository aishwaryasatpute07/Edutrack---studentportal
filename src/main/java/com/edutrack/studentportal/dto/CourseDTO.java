package com.edutrack.studentportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class CourseDTO {

    private String id;

    @NotBlank(message = "course name is required")
    private  String courseName;

    @NotBlank(message = "duration is required")
    private String duration;

    @Positive(message = "fees should be greater than 0")
    private double fees;

    @NotBlank(message = "description must be required")
    private String description;

    public CourseDTO() {
    }

    public CourseDTO(String id, String courseName, String duration,
                     double fees, String description) {
        this.id = id;
        this.courseName = courseName;
        this.duration = duration;
        this.fees = fees;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public double getFees() {
        return fees;
    }

    public void setFees(double fees) {
        this.fees = fees;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
