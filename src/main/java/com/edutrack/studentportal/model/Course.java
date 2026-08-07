package com.edutrack.studentportal.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "courses")
public class Course {

    @Id
    private String id;

    private String courseName;

    private String duration;

    private double fees;

    private String description;

    public Course() {

    }


    // Parameterized Constructor
    public Course(String id, String courseName,
                  String duration, double fees,
                  String description) {

        this.id = id;
        this.courseName = courseName;
        this.duration = duration;
        this.fees = fees;
        this.description = description;
    }


    // Getters and Setters

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
