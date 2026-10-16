package com.edutrack.studentportal.dto;

import java.util.ArrayList;
import java.util.List;

public class PerformanceSummaryDTO {

    private String courseId;

    private String courseName;

    private String instructorName;

    private int enrollmentCount;

    private double averageScore;

    private double attendancePercentage;

    private double passRate;

    private List<PerformanceChartPointDTO> chartData = new ArrayList<>();

    public PerformanceSummaryDTO() {
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }

    public int getEnrollmentCount() {
        return enrollmentCount;
    }

    public void setEnrollmentCount(int enrollmentCount) {
        this.enrollmentCount = enrollmentCount;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public double getAttendancePercentage() {
        return attendancePercentage;
    }

    public void setAttendancePercentage(double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }

    public double getPassRate() {
        return passRate;
    }

    public void setPassRate(double passRate) {
        this.passRate = passRate;
    }

    public List<PerformanceChartPointDTO> getChartData() {
        return chartData;
    }

    public void setChartData(List<PerformanceChartPointDTO> chartData) {
        this.chartData = chartData;
    }
}
