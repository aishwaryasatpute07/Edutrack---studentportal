package com.edutrack.studentportal.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "attendance")
public class Attendance {

    @Id
    private String id;

    private String studentId;

    private String date;

    private String status;


    // No Argument Constructor
    public Attendance() {

    }


    // Parameterized Constructor
    public Attendance(String id, String studentId,
                      String date, String status) {

        this.id = id;
        this.studentId = studentId;
        this.date = date;
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


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}