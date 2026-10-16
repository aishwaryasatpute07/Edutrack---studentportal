package com.edutrack.studentportal.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "academic_records")
public class AcademicRecord {

    @Id
    private String id;
    private String studentId;
    private String courseId;
    private String subjectId;
    private String examination;
    private int attemptNumber = 1;
    private double marksObtained;
    private double maximumMarks;
    private LocalDateTime updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }
    public String getExamination() { return examination; }
    public void setExamination(String examination) { this.examination = examination; }
    public int getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(int attemptNumber) { this.attemptNumber = attemptNumber; }
    public double getMarksObtained() { return marksObtained; }
    public void setMarksObtained(double marksObtained) { this.marksObtained = marksObtained; }
    public double getMaximumMarks() { return maximumMarks; }
    public void setMaximumMarks(double maximumMarks) { this.maximumMarks = maximumMarks; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public boolean isPassed() { return marksObtained / maximumMarks >= 0.5; }
    public boolean isBacklog() { return !isPassed(); }
}
