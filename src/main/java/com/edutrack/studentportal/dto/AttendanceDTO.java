package com.edutrack.studentportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class AttendanceDTO {

    private String id;

    @NotBlank(message = "Student ID is required")
    private String studentId;

    @NotNull(message = "Attendance date is required")
    private LocalDate date;

    @NotBlank(message = "Attendance status is required")
    private String status;

    public AttendanceDTO() {
    }

    public AttendanceDTO(String id, String studentId,
                         LocalDate date, String status) {
        this.id = id;
        this.studentId = studentId;
        this.date = date;
        this.status = status;
    }

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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}