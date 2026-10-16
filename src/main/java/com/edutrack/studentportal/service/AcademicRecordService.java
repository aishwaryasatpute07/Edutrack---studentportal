package com.edutrack.studentportal.service;

import com.edutrack.studentportal.dto.AcademicRecordDTO;
import com.edutrack.studentportal.exception.DuplicateResourceException;
import com.edutrack.studentportal.exception.InvalidRequestException;
import com.edutrack.studentportal.exception.ResourceNotFoundException;
import com.edutrack.studentportal.model.AcademicRecord;
import com.edutrack.studentportal.model.Course;
import com.edutrack.studentportal.model.Enrollment;
import com.edutrack.studentportal.repository.AcademicRecordRepository;
import com.edutrack.studentportal.repository.CourseRepository;
import com.edutrack.studentportal.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AcademicRecordService {

    private final AcademicRecordRepository academicRecordRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentAccessService studentAccessService;

    public AcademicRecordService(AcademicRecordRepository academicRecordRepository,
                                 CourseRepository courseRepository,
                                 EnrollmentRepository enrollmentRepository,
                                 StudentAccessService studentAccessService) {
        this.academicRecordRepository = academicRecordRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.studentAccessService = studentAccessService;
    }

    public List<AcademicRecordDTO> getAll() {
        studentAccessService.requireAdministrator();
        return academicRecordRepository.findAll().stream().map(this::toDTO).toList();
    }

    public List<AcademicRecordDTO> getByStudent(String studentId) {
        studentAccessService.requireStudentAccess(studentId);
        return academicRecordRepository.findByStudentIdOrderByUpdatedAtDesc(studentId)
                .stream().map(this::toDTO).toList();
    }

    public AcademicRecordDTO save(AcademicRecordDTO request) {
        studentAccessService.requireAcademicWriteAccess(request.getStudentId());
        validate(request);
        if (academicRecordRepository.existsByStudentIdAndCourseIdAndSubjectIdAndExaminationAndAttemptNumber(
                request.getStudentId(), request.getCourseId(), request.getSubjectId(),
                request.getExamination(), request.getAttemptNumber())) {
            throw new DuplicateResourceException("A result already exists for this student, subject and examination");
        }
        AcademicRecord record = new AcademicRecord();
        copyRequest(request, record);
        record.setUpdatedAt(LocalDateTime.now());
        return toDTO(academicRecordRepository.save(record));
    }

    public AcademicRecordDTO update(String id, AcademicRecordDTO request) {
        AcademicRecord record = academicRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic record not found: " + id));
        studentAccessService.requireAcademicWriteAccess(record.getStudentId());
        if (!record.getStudentId().equals(request.getStudentId())) {
            throw new InvalidRequestException("An academic record cannot be reassigned to another student");
        }
        validate(request);
        if (academicRecordRepository.existsByStudentIdAndCourseIdAndSubjectIdAndExaminationAndAttemptNumberAndIdNot(
                request.getStudentId(), request.getCourseId(), request.getSubjectId(),
                request.getExamination(), request.getAttemptNumber(), id)) {
            throw new DuplicateResourceException("A result already exists for this student, subject and examination");
        }
        copyRequest(request, record);
        record.setUpdatedAt(LocalDateTime.now());
        return toDTO(academicRecordRepository.save(record));
    }

    public void delete(String id) {
        studentAccessService.requireAdministrator();
        AcademicRecord record = academicRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic record not found: " + id));
        academicRecordRepository.delete(record);
    }

    private void validate(AcademicRecordDTO request) {
        if (request.getMaximumMarks() <= 0 || request.getMarksObtained() < 0
                || request.getMarksObtained() > request.getMaximumMarks()) {
            throw new InvalidRequestException("Marks must be between zero and the maximum marks");
        }
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + request.getCourseId()));
        boolean subjectBelongsToCourse = request.getCourseId().equals(request.getSubjectId())
                || courseRepository.findByParentCourseId(course.getId()).stream()
                .anyMatch(subject -> subject.getId().equals(request.getSubjectId()));
        if (!subjectBelongsToCourse) {
            throw new InvalidRequestException("Subject must be the selected course or one of its modules");
        }
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(
                request.getStudentId(), request.getCourseId())
                .orElseThrow(() -> new InvalidRequestException("Student is not enrolled in this course"));
        if ("CANCELLED".equalsIgnoreCase(enrollment.getStatus())) {
            throw new InvalidRequestException("Results cannot be recorded for a cancelled enrollment");
        }
    }

    private void copyRequest(AcademicRecordDTO source, AcademicRecord target) {
        target.setStudentId(source.getStudentId());
        target.setCourseId(source.getCourseId());
        target.setSubjectId(source.getSubjectId());
        target.setExamination(source.getExamination().trim());
        target.setAttemptNumber(source.getAttemptNumber());
        target.setMarksObtained(source.getMarksObtained());
        target.setMaximumMarks(source.getMaximumMarks());
    }

    private AcademicRecordDTO toDTO(AcademicRecord record) {
        AcademicRecordDTO dto = new AcademicRecordDTO();
        dto.setId(record.getId());
        dto.setStudentId(record.getStudentId());
        dto.setCourseId(record.getCourseId());
        dto.setSubjectId(record.getSubjectId());
        dto.setExamination(record.getExamination());
        dto.setAttemptNumber(record.getAttemptNumber());
        dto.setMarksObtained(record.getMarksObtained());
        dto.setMaximumMarks(record.getMaximumMarks());
        dto.setUpdatedAt(record.getUpdatedAt());
        dto.setPassed(record.isPassed());
        dto.setBacklog(record.isBacklog());
        return dto;
    }
}
