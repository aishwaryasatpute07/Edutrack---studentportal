package com.edutrack.studentportal.controller;

import com.edutrack.studentportal.dto.AttendanceDTO;
import com.edutrack.studentportal.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
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
}