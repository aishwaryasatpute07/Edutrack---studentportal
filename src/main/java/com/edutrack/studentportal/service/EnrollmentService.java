package com.edutrack.studentportal.service;

import com.edutrack.studentportal.dto.EnrollmentDTO;
import com.edutrack.studentportal.dto.EnrollmentFeeUpdateDTO;
import com.edutrack.studentportal.exception.DuplicateResourceException;
import com.edutrack.studentportal.exception.InvalidRequestException;
import com.edutrack.studentportal.exception.ResourceNotFoundException;
import com.edutrack.studentportal.model.Course;
import com.edutrack.studentportal.model.Enrollment;
import com.edutrack.studentportal.model.Student;
import com.edutrack.studentportal.repository.CourseRepository;
import com.edutrack.studentportal.repository.EnrollmentRepository;
import com.edutrack.studentportal.repository.StudentRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final ModelMapper modelMapper;
    private final CourseAuditService courseAuditService;
    private final StudentAccessService studentAccessService;
    private final PaymentService paymentService;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                            StudentRepository studentRepository,
                            CourseRepository courseRepository,
                            ModelMapper modelMapper,
                            CourseAuditService courseAuditService,
                            StudentAccessService studentAccessService,
                            PaymentService paymentService) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.modelMapper = modelMapper;
        this.courseAuditService = courseAuditService;
        this.studentAccessService = studentAccessService;
        this.paymentService = paymentService;
    }

    public EnrollmentDTO enrollStudent(String studentId, String courseId) {
        studentAccessService.requireAdministrator();
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id : " + studentId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id : " + courseId));

        if (enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId).isPresent()) {
            throw new DuplicateResourceException("Student is already enrolled in this course");
        }

        if (course.getCapacity() != null && course.getEnrolledStudents() >= course.getCapacity()) {
            throw new InvalidRequestException("Course has reached its maximum enrollment capacity");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(student.getId());
        enrollment.setCourseId(course.getId());
        enrollment.setStatus("ENROLLED");
        enrollment.setProgress(0.0);
        enrollment.setAttendancePercentage(0.0);
        enrollment.setAverageScore(0.0);
        enrollment.setFeeStatus("PENDING");

        course.setEnrolledStudents(course.getEnrolledStudents() + 1);
        courseRepository.save(course);

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        courseAuditService.record("ENROLLMENT_CREATED", course.getId(), course.getCourseName(),
            "Student enrolled: " + student.getId());
        return modelMapper.map(savedEnrollment, EnrollmentDTO.class);
    }

    public List<EnrollmentDTO> getEnrollmentsByCourse(String courseId) {
        studentAccessService.requireAdministrator();
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        return enrollments.stream()
                .map(enrollment -> modelMapper.map(enrollment, EnrollmentDTO.class))
                .toList();
    }

    public List<EnrollmentDTO> getEnrollmentsByStudent(String studentId) {
        studentAccessService.requireStudentAccess(studentId);
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        var feeSummaries = paymentService.getStudentFeeSummary(studentId);
        return enrollments.stream()
                .map(enrollment -> {
                    EnrollmentDTO dto = modelMapper.map(enrollment, EnrollmentDTO.class);
                    feeSummaries.stream()
                            .filter(summary -> summary.getCourseId().equals(enrollment.getCourseId()))
                            .findFirst()
                            .ifPresent(summary -> dto.setFeeStatus(summary.getPaymentStatus()));
                    return dto;
                })
                .toList();
    }

    public EnrollmentDTO updateFeeStructure(String enrollmentId, EnrollmentFeeUpdateDTO request) {
        studentAccessService.requireAdministrator();
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + enrollmentId));
        Course course = courseRepository.findById(enrollment.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + enrollment.getCourseId()));
        double subjectFees = courseRepository.findByParentCourseId(course.getId()).stream()
                .mapToDouble(Course::getFees).sum();
        double subtotal = course.getFees() + subjectFees + request.getSportsFee()
                + request.getExaminationFee() + request.getOtherCharges();
        if (request.getDiscount() > subtotal) {
            throw new InvalidRequestException("Discount cannot exceed the total fees");
        }

        enrollment.setSportsFee(request.getSportsFee());
        enrollment.setExaminationFee(request.getExaminationFee());
        enrollment.setOtherCharges(request.getOtherCharges());
        enrollment.setDiscount(request.getDiscount());
        return modelMapper.map(enrollmentRepository.save(enrollment), EnrollmentDTO.class);
    }

    public EnrollmentDTO updateEnrollmentStatus(String enrollmentId, String status) {
        studentAccessService.requireAdministrator();
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id : " + enrollmentId));

        if (status == null || status.isBlank()) {
            throw new InvalidRequestException("Status is required");
        }

        String normalizedStatus = status.toUpperCase();
        if (!List.of("ENROLLED", "COMPLETED", "CANCELLED").contains(normalizedStatus)) {
            throw new InvalidRequestException("Status must be ENROLLED, COMPLETED, or CANCELLED");
        }
        enrollment.setStatus(normalizedStatus);
        Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);
        Course course = courseRepository.findById(enrollment.getCourseId()).orElse(null);
        courseAuditService.record("ENROLLMENT_STATUS_UPDATED", enrollment.getCourseId(),
            course == null ? null : course.getCourseName(),
            "Enrollment " + enrollment.getId() + " status changed to " + updatedEnrollment.getStatus());
        return modelMapper.map(updatedEnrollment, EnrollmentDTO.class);
    }

    public void cancelEnrollment(String enrollmentId) {
        studentAccessService.requireAdministrator();
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id : " + enrollmentId));

        if ("CANCELLED".equalsIgnoreCase(enrollment.getStatus())) {
            throw new InvalidRequestException("Enrollment is already cancelled");
        }

        enrollment.setStatus("CANCELLED");
        enrollment.setFeeStatus("CANCELLED");
        enrollmentRepository.save(enrollment);
        Course course = courseRepository.findById(enrollment.getCourseId()).orElse(null);
        courseAuditService.record("ENROLLMENT_CANCELLED", enrollment.getCourseId(),
            course == null ? null : course.getCourseName(),
            "Enrollment cancelled for student " + enrollment.getStudentId());
    }
}
