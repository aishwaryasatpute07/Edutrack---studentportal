package com.edutrack.studentportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;

public class AcademicRecordDTO {

    private String id;
    @NotBlank
    private String studentId;
    @NotBlank
    private String courseId;
    @NotBlank
    private String subjectId;
    @NotBlank
    private String examination;
    @Min(value = 1, message = "Attempt must be at least 1")
    private int attemptNumber = 1;
    @PositiveOrZero
    private double marksObtained;
    @Positive
    private double maximumMarks;
    private LocalDateTime updatedAt;
    private boolean passed;
    private boolean backlog;

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
    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }
    public boolean isBacklog() { return backlog; }
    public void setBacklog(boolean backlog) { this.backlog = backlog; }
}
