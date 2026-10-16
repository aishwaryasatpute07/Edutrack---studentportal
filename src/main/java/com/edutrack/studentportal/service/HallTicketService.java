package com.edutrack.studentportal.service;

import com.edutrack.studentportal.dto.HallTicketDTO;
import com.edutrack.studentportal.exception.DuplicateResourceException;
import com.edutrack.studentportal.exception.InvalidRequestException;
import com.edutrack.studentportal.exception.ResourceNotFoundException;
import com.edutrack.studentportal.model.Course;
import com.edutrack.studentportal.model.Enrollment;
import com.edutrack.studentportal.model.HallTicket;
import com.edutrack.studentportal.model.Student;
import com.edutrack.studentportal.repository.CourseRepository;
import com.edutrack.studentportal.repository.EnrollmentRepository;
import com.edutrack.studentportal.repository.HallTicketRepository;
import com.edutrack.studentportal.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.List;

@Service
public class HallTicketService {

    private final HallTicketRepository hallTicketRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentAccessService studentAccessService;

    public HallTicketService(HallTicketRepository hallTicketRepository,
                             StudentRepository studentRepository,
                             CourseRepository courseRepository,
                             EnrollmentRepository enrollmentRepository,
                             StudentAccessService studentAccessService) {
        this.hallTicketRepository = hallTicketRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.studentAccessService = studentAccessService;
    }

    public List<HallTicketDTO> getAll() {
        studentAccessService.requireAdministrator();
        return hallTicketRepository.findAll().stream().map(this::toDTO).toList();
    }

    public List<HallTicketDTO> getByStudent(String studentId) {
        studentAccessService.requireStudentAccess(studentId);
        return hallTicketRepository.findByStudentIdOrderByExamDateAscStartTimeAsc(studentId)
                .stream().map(this::toDTO).toList();
    }

    public HallTicketDTO getForDownload(String id) {
        HallTicket ticket = hallTicketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hall ticket not found: " + id));
        studentAccessService.requireStudentAccess(ticket.getStudentId());
        return toDTO(ticket);
    }

    public HallTicketDTO create(HallTicketDTO request) {
        studentAccessService.requireAdministrator();
        validate(request, null);
        HallTicket ticket = new HallTicket();
        copy(request, ticket);
        ticket.setIssuedAt(LocalDateTime.now());
        return saveTicket(ticket);
    }

    public HallTicketDTO update(String id, HallTicketDTO request) {
        studentAccessService.requireAdministrator();
        HallTicket ticket = hallTicketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hall ticket not found: " + id));
        validate(request, id);
        copy(request, ticket);
        return saveTicket(ticket);
    }

    public void delete(String id) {
        studentAccessService.requireAdministrator();
        HallTicket ticket = hallTicketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hall ticket not found: " + id));
        hallTicketRepository.delete(ticket);
    }

    private void validate(HallTicketDTO request, String currentId) {
        studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + request.getStudentId()));
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + request.getCourseId()));
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(
                        request.getStudentId(), course.getId())
                .orElseThrow(() -> new InvalidRequestException("Student is not enrolled in this course"));
        if ("CANCELLED".equalsIgnoreCase(enrollment.getStatus())) {
            throw new InvalidRequestException("Hall tickets cannot be issued for a cancelled enrollment");
        }
        boolean duplicate = hallTicketRepository
                .existsByCourseIdAndExaminationIgnoreCaseAndExamDateAndSeatNumberIgnoreCaseAndIdNot(
                        course.getId(), request.getExamination().trim(), request.getExamDate(),
                        request.getSeatNumber().trim(), currentId);
        if (duplicate) {
            throw new DuplicateResourceException("This seat is already assigned for the selected examination");
        }
    }

    private void copy(HallTicketDTO source, HallTicket target) {
        target.setStudentId(source.getStudentId());
        target.setCourseId(source.getCourseId());
        target.setExamination(source.getExamination().trim());
        target.setExaminationKey(source.getExamination().trim().toLowerCase(Locale.ROOT));
        target.setExamDate(source.getExamDate());
        target.setStartTime(source.getStartTime());
        target.setVenue(source.getVenue().trim());
        target.setSeatNumber(source.getSeatNumber().trim());
        target.setSeatNumberKey(source.getSeatNumber().trim().toLowerCase(Locale.ROOT));
        target.setInstructions(source.getInstructions() == null ? "" : source.getInstructions().trim());
    }

    private HallTicketDTO saveTicket(HallTicket ticket) {
        try {
            return toDTO(hallTicketRepository.save(ticket));
        } catch (DuplicateKeyException exception) {
            throw new DuplicateResourceException("This seat is already assigned for the selected examination");
        }
    }

    private HallTicketDTO toDTO(HallTicket ticket) {
        HallTicketDTO dto = new HallTicketDTO();
        dto.setId(ticket.getId());
        dto.setStudentId(ticket.getStudentId());
        dto.setCourseId(ticket.getCourseId());
        dto.setExamination(ticket.getExamination());
        dto.setExamDate(ticket.getExamDate());
        dto.setStartTime(ticket.getStartTime());
        dto.setVenue(ticket.getVenue());
        dto.setSeatNumber(ticket.getSeatNumber());
        dto.setInstructions(ticket.getInstructions());
        dto.setIssuedAt(ticket.getIssuedAt());
        studentRepository.findById(ticket.getStudentId()).map(Student::getName).ifPresent(dto::setStudentName);
        courseRepository.findById(ticket.getCourseId()).map(Course::getCourseName).ifPresent(dto::setCourseName);
        return dto;
    }
}
