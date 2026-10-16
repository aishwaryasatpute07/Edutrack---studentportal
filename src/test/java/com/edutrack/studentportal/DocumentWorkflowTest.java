package com.edutrack.studentportal;

import com.edutrack.studentportal.dto.HallTicketDTO;
import com.edutrack.studentportal.exception.InvalidRequestException;
import com.edutrack.studentportal.model.Course;
import com.edutrack.studentportal.model.Enrollment;
import com.edutrack.studentportal.model.HallTicket;
import com.edutrack.studentportal.model.Student;
import com.edutrack.studentportal.repository.CourseRepository;
import com.edutrack.studentportal.repository.EnrollmentRepository;
import com.edutrack.studentportal.repository.HallTicketRepository;
import com.edutrack.studentportal.repository.StudentRepository;
import com.edutrack.studentportal.service.HallTicketService;
import com.edutrack.studentportal.service.PdfDocumentService;
import com.edutrack.studentportal.service.StudentAccessService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DocumentWorkflowTest {

    @Test
    void generatedPdfHasValidPdfStructureAndContent() throws Exception {
        byte[] pdf = new PdfDocumentService().create(
                "Student Marks", List.of("Student ID: student-1", "Marks: 88.00 / 100"));

        assertTrue(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII).startsWith("%PDF-"));
        try (var document = Loader.loadPDF(pdf)) {
            assertEquals(1, document.getNumberOfPages());
            assertTrue(new PDFTextStripper().getText(document).contains("Student ID: student-1"));
        }
    }

    @Test
    void hallTicketCanOnlyBeIssuedForAnActiveCourseEnrollment() {
        HallTicketRepository ticketRepository = mock(HallTicketRepository.class);
        StudentRepository studentRepository = mock(StudentRepository.class);
        CourseRepository courseRepository = mock(CourseRepository.class);
        EnrollmentRepository enrollmentRepository = mock(EnrollmentRepository.class);
        StudentAccessService accessService = mock(StudentAccessService.class);
        Student student = new Student();
        student.setId("student-1");
        student.setName("Student One");
        Course course = new Course();
        course.setId("course-1");
        course.setCourseName("Course One");
        Enrollment enrollment = new Enrollment();
        enrollment.setStatus("ENROLLED");
        when(studentRepository.findById("student-1")).thenReturn(Optional.of(student));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByStudentIdAndCourseId("student-1", "course-1"))
                .thenReturn(Optional.of(enrollment));
        when(ticketRepository.save(any(HallTicket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        HallTicketService service = new HallTicketService(ticketRepository, studentRepository,
                courseRepository, enrollmentRepository, accessService);

        HallTicketDTO saved = service.create(ticketRequest());

        assertEquals("Student One", saved.getStudentName());
        assertEquals("Course One", saved.getCourseName());
        assertEquals("A-14", saved.getSeatNumber());
        assertTrue(saved.getIssuedAt() != null);
        verify(accessService).requireAdministrator();
    }

    @Test
    void hallTicketCannotBeIssuedForCancelledEnrollment() {
        HallTicketRepository ticketRepository = mock(HallTicketRepository.class);
        StudentRepository studentRepository = mock(StudentRepository.class);
        CourseRepository courseRepository = mock(CourseRepository.class);
        EnrollmentRepository enrollmentRepository = mock(EnrollmentRepository.class);
        StudentAccessService accessService = mock(StudentAccessService.class);
        when(studentRepository.findById("student-1")).thenReturn(Optional.of(new Student()));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(new Course()));
        Enrollment enrollment = new Enrollment();
        enrollment.setStatus("CANCELLED");
        when(enrollmentRepository.findByStudentIdAndCourseId("student-1", "course-1"))
                .thenReturn(Optional.of(enrollment));
        HallTicketService service = new HallTicketService(ticketRepository, studentRepository,
                courseRepository, enrollmentRepository, accessService);

        assertThrows(InvalidRequestException.class, () -> service.create(ticketRequest()));
    }

    private HallTicketDTO ticketRequest() {
        HallTicketDTO request = new HallTicketDTO();
        request.setStudentId("student-1");
        request.setCourseId("course-1");
        request.setExamination("Semester 1");
        request.setExamDate(LocalDate.of(2026, 11, 20));
        request.setStartTime(LocalTime.of(9, 30));
        request.setVenue("Main Hall");
        request.setSeatNumber("A-14");
        request.setInstructions("Bring an identity card.");
        return request;
    }
}
