package com.edutrack.studentportal.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "students")
public class Student {

    @Id
    private String id ;

    @NotBlank(message = "name is required")
    private String name ;

    @NotBlank(message = "email is required")
    @Email(message = "email should be valid")
    private String email;

    private String mobile;

    private double fees;

    private String batchId;

    public Student(){

    }

    public Student(String id, String name, String email,
                   String mobile,
                   String batchId, double fees) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.batchId = batchId;
        this.fees = fees;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }


    public double getFees() {
        return fees;
    }

    public void setFees(double fees) {
        this.fees = fees;
    }
}

