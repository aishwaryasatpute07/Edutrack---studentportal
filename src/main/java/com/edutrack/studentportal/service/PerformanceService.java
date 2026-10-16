package com.edutrack.studentportal.service;

import com.edutrack.studentportal.dto.PerformanceChartPointDTO;
import com.edutrack.studentportal.dto.PerformanceSummaryDTO;
import com.edutrack.studentportal.exception.ResourceNotFoundException;
import com.edutrack.studentportal.model.Course;
import com.edutrack.studentportal.model.Enrollment;
import com.edutrack.studentportal.repository.CourseRepository;
import com.edutrack.studentportal.repository.AcademicRecordRepository;
import com.edutrack.studentportal.repository.AttendanceRepository;
import com.edutrack.studentportal.repository.EnrollmentRepository;
import com.edutrack.studentportal.model.AcademicRecord;
import com.edutrack.studentportal.model.Attendance;
import com.edutrack.studentportal.model.Student;
import com.edutrack.studentportal.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PerformanceService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentAccessService studentAccessService;
    private final AcademicRecordRepository academicRecordRepository;
    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;

    public PerformanceService(CourseRepository courseRepository,
                             EnrollmentRepository enrollmentRepository,
                             StudentAccessService studentAccessService,
                             AcademicRecordRepository academicRecordRepository,
                             AttendanceRepository attendanceRepository,
                             StudentRepository studentRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.studentAccessService = studentAccessService;
        this.academicRecordRepository = academicRecordRepository;
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
    }

    public PerformanceSummaryDTO getCoursePerformance(String courseId) {
        studentAccessService.requireAdministrator();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id : " + courseId));

        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);

        List<AcademicRecord> records = academicRecordRepository.findByCourseId(courseId);
        double averageScore = scorePercentage(records);
        List<String> studentIds = enrollments.stream().map(Enrollment::getStudentId).toList();
        double attendancePercentage = attendancePercentage(attendanceRepository.findByStudentIdIn(studentIds));
        long passed = records.stream().filter(AcademicRecord::isPassed).count();
        double passRate = records.isEmpty() ? 0.0 : passed * 100.0 / records.size();

        List<PerformanceChartPointDTO> chartData = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            List<AcademicRecord> studentRecords = records.stream()
                    .filter(record -> enrollment.getStudentId().equals(record.getStudentId()))
                    .toList();
            Student student = studentRepository.findById(enrollment.getStudentId()).orElse(null);
            chartData.add(new PerformanceChartPointDTO(
                    student == null ? enrollment.getStudentId() : student.getName(),
                    scorePercentage(studentRecords)));
        }

        PerformanceSummaryDTO summary = new PerformanceSummaryDTO();
        summary.setCourseId(course.getId());
        summary.setCourseName(course.getCourseName());
        summary.setInstructorName(course.getInstructorName());
        summary.setEnrollmentCount(enrollments.size());
        summary.setAverageScore(averageScore);
        summary.setAttendancePercentage(attendancePercentage);
        summary.setPassRate(passRate);
        summary.setChartData(chartData);
        return summary;
    }

    public PerformanceSummaryDTO getStudentPerformance(String studentId) {
        studentAccessService.requireStudentAccess(studentId);
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        List<AcademicRecord> records = academicRecordRepository.findByStudentId(studentId);
        Map<String, List<AcademicRecord>> recordsByCourse = new HashMap<>();
        for (AcademicRecord record : records) {
            recordsByCourse.computeIfAbsent(record.getCourseId(), ignored -> new ArrayList<>()).add(record);
        }

        List<PerformanceChartPointDTO> chartData = new ArrayList<>();
        for (Map.Entry<String, List<AcademicRecord>> entry : recordsByCourse.entrySet()) {
            Course course = courseRepository.findById(entry.getKey()).orElse(null);
            chartData.add(new PerformanceChartPointDTO(
                    course == null ? entry.getKey() : course.getCourseName(),
                    scorePercentage(entry.getValue())));
        }

        PerformanceSummaryDTO summary = new PerformanceSummaryDTO();
        summary.setCourseId(enrollments.isEmpty() ? null : enrollments.get(0).getCourseId());
        summary.setCourseName("Student performance summary");
        summary.setInstructorName("N/A");
        summary.setEnrollmentCount(enrollments.size());
        summary.setAverageScore(scorePercentage(records));
        summary.setAttendancePercentage(attendancePercentage(attendanceRepository.findByStudentId(studentId)));
        long passed = records.stream().filter(AcademicRecord::isPassed).count();
        summary.setPassRate(records.isEmpty() ? 0.0 : passed * 100.0 / records.size());
        summary.setChartData(chartData);
        return summary;
    }

    private double scorePercentage(List<AcademicRecord> records) {
        double totalMaximum = records.stream().mapToDouble(AcademicRecord::getMaximumMarks).sum();
        if (totalMaximum <= 0) {
            return 0;
        }
        return records.stream().mapToDouble(AcademicRecord::getMarksObtained).sum() * 100.0 / totalMaximum;
    }

    private double attendancePercentage(List<Attendance> records) {
        if (records.isEmpty()) {
            return 0;
        }
        long present = records.stream().filter(record -> "PRESENT".equalsIgnoreCase(record.getStatus())).count();
        return present * 100.0 / records.size();
    }
}
