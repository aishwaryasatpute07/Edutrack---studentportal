package com.edutrack.studentportal.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Document(collection = "hall_tickets")
@CompoundIndex(name = "hall_ticket_seat_per_exam",
        def = "{'courseId': 1, 'examDate': 1, 'examinationKey': 1, 'seatNumberKey': 1}", unique = true)
public class HallTicket {

    @Id
    private String id;

    private String studentId;

    private String courseId;

    private String examination;

    private String examinationKey;

    private LocalDate examDate;

    private LocalTime startTime;

    private String venue;

    private String seatNumber;

    private String seatNumberKey;

    private String instructions;

    private LocalDateTime issuedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public String getExamination() { return examination; }
    public void setExamination(String examination) { this.examination = examination; }
    public String getExaminationKey() { return examinationKey; }
    public void setExaminationKey(String examinationKey) { this.examinationKey = examinationKey; }
    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) { this.examDate = examDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }
    public String getSeatNumberKey() { return seatNumberKey; }
    public void setSeatNumberKey(String seatNumberKey) { this.seatNumberKey = seatNumberKey; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
}
