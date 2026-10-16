package com.edutrack.studentportal;

import com.edutrack.studentportal.config.RazorpayConfig;
import com.edutrack.studentportal.dto.AcademicRecordDTO;
import com.edutrack.studentportal.dto.FeeSummaryDTO;
import com.edutrack.studentportal.dto.VerifyPaymentDTO;
import com.edutrack.studentportal.exception.InvalidRequestException;
import com.edutrack.studentportal.model.AcademicRecord;
import com.edutrack.studentportal.model.Course;
import com.edutrack.studentportal.model.Enrollment;
import com.edutrack.studentportal.model.Payment;
import com.edutrack.studentportal.repository.AcademicRecordRepository;
import com.edutrack.studentportal.repository.CourseRepository;
import com.edutrack.studentportal.repository.EnrollmentRepository;
import com.edutrack.studentportal.repository.PaymentRepository;
import com.edutrack.studentportal.service.AcademicRecordService;
import com.edutrack.studentportal.service.CourseAuditService;
import com.edutrack.studentportal.service.PaymentService;
import com.edutrack.studentportal.service.RazorpayService;
import com.edutrack.studentportal.service.StudentAccessService;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class StudentWorkflowTest {

    @Test
    void feeSummaryIncludesFeeComponentsAndVerifiedPayments() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        CourseRepository courseRepository = mock(CourseRepository.class);
        EnrollmentRepository enrollmentRepository = mock(EnrollmentRepository.class);
        String studentId = "student-1";
        String courseId = "course-1";

        Course course = new Course();
        course.setId(courseId);
        course.setCourseName("Course");
        course.setFees(1000);
        Course subject = new Course();
        subject.setId("subject-1");
        subject.setCourseName("Subject");
        subject.setFees(200);
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(studentId);
        enrollment.setCourseId(courseId);
        enrollment.setSportsFee(50);
        enrollment.setExaminationFee(20);
        enrollment.setOtherCharges(10);
        enrollment.setDiscount(80);
        Payment verifiedPayment = new Payment();
        verifiedPayment.setAmount(400);

        when(enrollmentRepository.findByStudentId(studentId)).thenReturn(List.of(enrollment));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseRepository.findByParentCourseId(courseId)).thenReturn(List.of(subject));
        when(paymentRepository.findByStudentIdAndCourseIdAndStatusIgnoreCase(studentId, courseId, "VERIFIED"))
                .thenReturn(List.of(verifiedPayment));

        PaymentService service = new PaymentService(
                paymentRepository,
                courseRepository,
                enrollmentRepository,
                mock(org.modelmapper.ModelMapper.class),
                mock(RazorpayService.class),
                new RazorpayConfig.RazorpayProperties("key", "secret", ""),
                mock(CourseAuditService.class),
                mock(StudentAccessService.class));

        List<FeeSummaryDTO> summary = service.getStudentFeeSummary(studentId);

        assertEquals(1, summary.size());
        assertEquals(1200.0, summary.get(0).getTotalFee(), 0.001);
        assertEquals(400.0, summary.get(0).getPaidAmount(), 0.001);
        assertEquals(800.0, summary.get(0).getPendingAmount(), 0.001);
        assertEquals("PARTIALLY_PAID", summary.get(0).getPaymentStatus());
        assertEquals(200.0, summary.get(0).getSubjectFees().get("Subject"), 0.001);
    }

    @Test
    void academicRecordRejectsMarksAboveMaximum() {
        AcademicRecordRepository recordRepository = mock(AcademicRecordRepository.class);
        AcademicRecordService service = new AcademicRecordService(
                recordRepository,
                mock(CourseRepository.class),
                mock(EnrollmentRepository.class),
                mock(StudentAccessService.class));
        AcademicRecordDTO request = recordRequest(101, 100);

        assertThrows(InvalidRequestException.class, () -> service.save(request));
    }

    @Test
    void academicRecordStoresAttemptAndDerivesBacklogFromMarks() {
        AcademicRecordRepository recordRepository = mock(AcademicRecordRepository.class);
        CourseRepository courseRepository = mock(CourseRepository.class);
        EnrollmentRepository enrollmentRepository = mock(EnrollmentRepository.class);
        Course course = new Course();
        course.setId("course-1");
        Enrollment enrollment = new Enrollment();
        enrollment.setStatus("ENROLLED");
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(courseRepository.findByParentCourseId("course-1")).thenReturn(List.of());
        when(enrollmentRepository.findByStudentIdAndCourseId("student-1", "course-1"))
                .thenReturn(Optional.of(enrollment));
        when(recordRepository.existsByStudentIdAndCourseIdAndSubjectIdAndExaminationAndAttemptNumber(
                "student-1", "course-1", "course-1", "Final", 2)).thenReturn(false);
        when(recordRepository.save(any(AcademicRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        AcademicRecordService service = new AcademicRecordService(
                recordRepository,
                courseRepository,
                enrollmentRepository,
                mock(StudentAccessService.class));
        AcademicRecordDTO request = recordRequest(45, 100);
        request.setAttemptNumber(2);
        request.setExamination(" Final ");

        AcademicRecordDTO saved = service.save(request);

        assertEquals(2, saved.getAttemptNumber());
        assertEquals("Final", saved.getExamination());
        assertFalse(saved.isPassed());
        assertTrue(saved.isBacklog());
        verify(recordRepository).save(any(AcademicRecord.class));
    }

    @Test
    void studentCannotVerifyAnotherStudentsPayment() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        RazorpayService razorpayService = mock(RazorpayService.class);
        StudentAccessService accessService = mock(StudentAccessService.class);
        Payment payment = new Payment();
        payment.setStudentId("student-2");
        payment.setStatus("CREATED");
        when(accessService.requireCurrentStudentId()).thenReturn("student-1");
        when(paymentRepository.findByOrderId("order-1")).thenReturn(payment);
        PaymentService service = new PaymentService(
                paymentRepository,
                mock(CourseRepository.class),
                mock(EnrollmentRepository.class),
                mock(org.modelmapper.ModelMapper.class),
                razorpayService,
                new RazorpayConfig.RazorpayProperties("key", "secret", ""),
                mock(CourseAuditService.class),
                accessService);
        VerifyPaymentDTO request = new VerifyPaymentDTO();
        request.setOrderId("order-1");
        request.setPaymentId("payment-1");
        request.setSignature("signature");

        assertThrows(AccessDeniedException.class, () -> service.verifyPayment(request));
        verifyNoInteractions(razorpayService);
    }

    private AcademicRecordDTO recordRequest(double marks, double maximum) {
        AcademicRecordDTO request = new AcademicRecordDTO();
        request.setStudentId("student-1");
        request.setCourseId("course-1");
        request.setSubjectId("course-1");
        request.setExamination("Final");
        request.setMarksObtained(marks);
        request.setMaximumMarks(maximum);
        return request;
    }
}
