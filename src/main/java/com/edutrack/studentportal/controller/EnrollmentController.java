package com.edutrack.studentportal.controller;

import com.edutrack.studentportal.dto.EnrollmentDTO;
import com.edutrack.studentportal.dto.EnrollmentFeeUpdateDTO;
import com.edutrack.studentportal.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    public EnrollmentDTO enrollStudent(@RequestParam String studentId,
                                      @RequestParam String courseId) {
        return enrollmentService.enrollStudent(studentId, courseId);
    }

    @GetMapping("/course/{courseId}")
    public List<EnrollmentDTO> getEnrollmentsByCourse(@PathVariable String courseId) {
        return enrollmentService.getEnrollmentsByCourse(courseId);
    }

    @GetMapping("/student/{studentId}")
    public List<EnrollmentDTO> getEnrollmentsByStudent(@PathVariable String studentId) {
        return enrollmentService.getEnrollmentsByStudent(studentId);
    }

    @PutMapping("/{id}/status")
    public EnrollmentDTO updateEnrollmentStatus(@PathVariable String id,
                                               @RequestParam String status) {
        return enrollmentService.updateEnrollmentStatus(id, status);
    }

    @PutMapping("/{id}/fees")
    public EnrollmentDTO updateFeeStructure(@PathVariable String id,
                                            @Valid @RequestBody EnrollmentFeeUpdateDTO request) {
        return enrollmentService.updateFeeStructure(id, request);
    }

    @DeleteMapping("/{id}")
    public String cancelEnrollment(@PathVariable String id) {
        enrollmentService.cancelEnrollment(id);
        return "Enrollment cancelled successfully";
    }
}
