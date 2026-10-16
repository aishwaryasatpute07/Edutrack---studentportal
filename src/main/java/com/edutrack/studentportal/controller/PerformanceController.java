package com.edutrack.studentportal.controller;

import com.edutrack.studentportal.dto.PerformanceSummaryDTO;
import com.edutrack.studentportal.service.PerformanceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
public class PerformanceController {

    private final PerformanceService performanceService;

    public PerformanceController(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    @GetMapping("/performance/course/{courseId}")
    public PerformanceSummaryDTO getCoursePerformance(@PathVariable String courseId) {
        return performanceService.getCoursePerformance(courseId);
    }

    @GetMapping("/performance/student/{studentId}")
    public PerformanceSummaryDTO getStudentPerformance(@PathVariable String studentId) {
        return performanceService.getStudentPerformance(studentId);
    }
}
