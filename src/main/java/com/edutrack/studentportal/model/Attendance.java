package com.edutrack.studentportal.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "attendance")
public class Attendance {

    @Id
    private String id;

    private String studentId;

    private LocalDate date;

    private String status;

    public Attendance() {
    }

    public Attendance(String id, String studentId,
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