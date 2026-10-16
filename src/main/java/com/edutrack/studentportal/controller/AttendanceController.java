package com.edutrack.studentportal.controller;

import com.edutrack.studentportal.dto.AttendanceDTO;
import com.edutrack.studentportal.service.AttendanceService;
import com.edutrack.studentportal.service.PdfDocumentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final PdfDocumentService pdfDocumentService;

    public AttendanceController(AttendanceService attendanceService,
                                PdfDocumentService pdfDocumentService) {
        this.attendanceService = attendanceService;
        this.pdfDocumentService = pdfDocumentService;
    }

    // Create attendance
    @PostMapping
    public AttendanceDTO saveAttendance(
            @Valid @RequestBody AttendanceDTO attendanceDTO) {

        return attendanceService.saveAttendance(attendanceDTO);
    }

    // Get all attendance
    @GetMapping
    public List<AttendanceDTO> getAllAttendance() {

        return attendanceService.getAllAttendance();
    }

    // Get attendance by ID
    @GetMapping("/{id}")
    public AttendanceDTO getAttendanceById(
            @PathVariable String id) {

        return attendanceService.getAttendanceById(id);
    }

    // Get attendance by student ID
    @GetMapping("/student/{studentId}")
    public List<AttendanceDTO> getAttendanceByStudentId(
            @PathVariable String studentId) {

        return attendanceService.getAttendanceByStudentId(studentId);
    }

    @GetMapping("/student/{studentId}/report")
    public ResponseEntity<byte[]> downloadStudentAttendanceReport(@PathVariable String studentId) {
        List<AttendanceDTO> records = attendanceService.getAttendanceByStudentId(studentId);
        long present = records.stream().filter(record -> "PRESENT".equalsIgnoreCase(record.getStatus())).count();
        long absent = records.stream().filter(record -> "ABSENT".equalsIgnoreCase(record.getStatus())).count();
        double percentage = records.isEmpty() ? 0 : present * 100.0 / records.size();
        StringBuilder csv = new StringBuilder("Student ID,Summary,Value\r\n")
                .append(csv(studentId, "Total working days", String.valueOf(records.size())))
                .append(csv(studentId, "Days present", String.valueOf(present)))
                .append(csv(studentId, "Days absent", String.valueOf(absent)))
                .append(csv(studentId, "Attendance percentage", String.format(java.util.Locale.ROOT, "%.2f%%", percentage)))
                .append("Date,Status\r\n");
        records.stream().sorted(java.util.Comparator.comparing(AttendanceDTO::getDate))
                .forEach(record -> csv.append(csv(record.getDate().toString(), record.getStatus())));
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("attendance-" + studentId.replaceAll("[^A-Za-z0-9_-]", "_") + ".csv")
                        .build().toString())
                .body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/student/{studentId}/report/pdf")
    public ResponseEntity<byte[]> downloadStudentAttendancePdf(@PathVariable String studentId) {
        List<AttendanceDTO> records = attendanceService.getAttendanceByStudentId(studentId);
        long present = records.stream().filter(record -> "PRESENT".equalsIgnoreCase(record.getStatus())).count();
        long absent = records.stream().filter(record -> "ABSENT".equalsIgnoreCase(record.getStatus())).count();
        double percentage = records.isEmpty() ? 0 : present * 100.0 / records.size();
        List<String> lines = new java.util.ArrayList<>(List.of(
                "Student ID: " + studentId,
                "Total working days: " + records.size(),
                "Days present: " + present,
                "Days absent: " + absent,
                String.format(java.util.Locale.ROOT, "Attendance percentage: %.2f%%", percentage),
                "",
                "Daily attendance:"));
        records.stream().sorted(java.util.Comparator.comparing(AttendanceDTO::getDate))
                .forEach(record -> lines.add(record.getDate() + " | " + record.getStatus()));
        byte[] pdf = pdfDocumentService.create("ATTENDANCE REPORT", lines);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("attendance-" + studentId.replaceAll("[^A-Za-z0-9_-]", "_") + ".pdf").build().toString())
                .body(pdf);
    }

    // Get attendance by date
    @GetMapping("/date/{date}")
    public List<AttendanceDTO> getAttendanceByDate(
            @PathVariable LocalDate date) {

        return attendanceService.getAttendanceByDate(date);
    }

    // Update attendance
    @PutMapping("/{id}")
    public AttendanceDTO updateAttendance(
            @PathVariable String id,
            @Valid @RequestBody AttendanceDTO attendanceDTO) {

        return attendanceService.updateAttendance(id, attendanceDTO);
    }

    // Delete attendance
    @DeleteMapping("/{id}")
    public String deleteAttendance(
            @PathVariable String id) {

        attendanceService.deleteAttendance(id);

        return "Attendance deleted successfully";
    }

    private String csv(String... values) {
        return java.util.Arrays.stream(values)
                .map(value -> "\"" + value.replace("\"", "\"\"") + "\"")
                .collect(java.util.stream.Collectors.joining(",")) + "\r\n";
    }
}