package com.edutrack.studentportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class CourseDTO {

    private String id;

    @NotBlank(message = "course name is required")
    private String courseName;

    @NotBlank(message = "duration is required")
    private String duration;

    @Positive(message = "fees should be greater than 0")
    private double fees;

    @NotBlank(message = "description must be required")
    private String description;

    private Map<String, String> descriptionTranslations = new HashMap<>();

    private String parentCourseId;

    private String status = "DRAFT";

    private String instructorName;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer capacity;

    private Integer enrolledStudents;

    public CourseDTO() {
    }

    public CourseDTO(String id, String courseName, String duration,
                    double fees, String description, String status,
                    String instructorName, LocalDate startDate,
                    LocalDate endDate, Integer capacity, Integer enrolledStudents) {
        this.id = id;
        this.courseName = courseName;
        this.duration = duration;
        this.fees = fees;
        this.description = description;
        this.status = status;
        this.instructorName = instructorName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.capacity = capacity;
        this.enrolledStudents = enrolledStudents;
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

    public Map<String, String> getDescriptionTranslations() {
        return descriptionTranslations == null ? Map.of() : descriptionTranslations;
    }

    public void setDescriptionTranslations(Map<String, String> descriptionTranslations) {
        this.descriptionTranslations = descriptionTranslations == null ? new HashMap<>() : new HashMap<>(descriptionTranslations);
    }

    public String getParentCourseId() {
        return parentCourseId;
    }

    public void setParentCourseId(String parentCourseId) {
        this.parentCourseId = parentCourseId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getEnrolledStudents() {
        return enrolledStudents == null ? 0 : enrolledStudents;
    }

    public void setEnrolledStudents(Integer enrolledStudents) {
        this.enrolledStudents = enrolledStudents;
    }
}
