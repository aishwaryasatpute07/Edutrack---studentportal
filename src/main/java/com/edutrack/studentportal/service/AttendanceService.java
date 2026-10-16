package com.edutrack.studentportal.service;

import com.edutrack.studentportal.dto.AttendanceDTO;
import com.edutrack.studentportal.exception.DuplicateResourceException;
import com.edutrack.studentportal.exception.ResourceNotFoundException;
import com.edutrack.studentportal.model.Attendance;
import com.edutrack.studentportal.model.Batch;
import com.edutrack.studentportal.model.Course;
import com.edutrack.studentportal.model.Student;
import com.edutrack.studentportal.repository.AttendanceRepository;
import com.edutrack.studentportal.repository.BatchRepository;
import com.edutrack.studentportal.repository.CourseRepository;
import com.edutrack.studentportal.repository.StudentRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final ModelMapper modelMapper;
        private final BatchRepository batchRepository;
        private final CourseRepository courseRepository;
        private final CourseAuditService courseAuditService;
        private final StudentAccessService studentAccessService;

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            StudentRepository studentRepository,
            ModelMapper modelMapper,
            BatchRepository batchRepository,
            CourseRepository courseRepository,
            CourseAuditService courseAuditService,
            StudentAccessService studentAccessService) {

        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.modelMapper = modelMapper;
        this.batchRepository = batchRepository;
        this.courseRepository = courseRepository;
        this.courseAuditService = courseAuditService;
        this.studentAccessService = studentAccessService;
    }

    // Create attendance
    public AttendanceDTO saveAttendance(AttendanceDTO attendanceDTO) {
        studentAccessService.requireAcademicWriteAccess(attendanceDTO.getStudentId());
        validateStatus(attendanceDTO.getStatus());

        // 1. Check student exists
        Student student = studentRepository.findById(
                attendanceDTO.getStudentId()
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Student not found with id : "
                                + attendanceDTO.getStudentId()
                )
        );

        // 2. Check student is assigned to a batch
        if (student.getBatchId() == null ||
                student.getBatchId().isBlank()) {

            throw new ResourceNotFoundException(
                    "Student is not assigned to any batch"
            );
        }

        // 3. Prevent duplicate attendance
        if (attendanceRepository.existsByStudentIdAndDate(
                attendanceDTO.getStudentId(),
                attendanceDTO.getDate())) {

            throw new DuplicateResourceException(
                    "Attendance already exists for this student on "
                            + attendanceDTO.getDate()
            );
        }

        // 4. DTO -> Entity
        Attendance attendance =
                modelMapper.map(attendanceDTO, Attendance.class);

        // 5. Save
        Attendance savedAttendance =
                attendanceRepository.save(attendance);
        recordCourseAttendance("ATTENDANCE_CREATED", student.getId(), savedAttendance.getStatus());

        // 6. Entity -> DTO
        return modelMapper.map(
                savedAttendance,
                AttendanceDTO.class
        );
    }

    // Get all attendance
    public List<AttendanceDTO> getAllAttendance() {
        studentAccessService.requireAdministrator();

        return attendanceRepository.findAll()
                .stream()
                .map(attendance ->
                        modelMapper.map(
                                attendance,
                                AttendanceDTO.class
                        ))
                .toList();
    }

    // Get attendance by ID
    public AttendanceDTO getAttendanceById(String id) {
        studentAccessService.requireAdministrator();

        Attendance attendance =
                attendanceRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Attendance not found with id : "
                                                + id
                                ));

        return modelMapper.map(
                attendance,
                AttendanceDTO.class
        );
    }

    // Get attendance by student ID
    public List<AttendanceDTO> getAttendanceByStudentId(
            String studentId) {
        studentAccessService.requireStudentAccess(studentId);

        // Check student exists
        if (!studentRepository.existsById(studentId)) {

            throw new ResourceNotFoundException(
                    "Student not found with id : " + studentId
            );
        }

        return attendanceRepository
                .findByStudentId(studentId)
                .stream()
                .map(attendance ->
                        modelMapper.map(
                                attendance,
                                AttendanceDTO.class
                        ))
                .toList();
    }

    // Get attendance by date
    public List<AttendanceDTO> getAttendanceByDate(
            java.time.LocalDate date) {
        studentAccessService.requireAdministrator();

        return attendanceRepository
                .findByDate(date)
                .stream()
                .map(attendance ->
                        modelMapper.map(
                                attendance,
                                AttendanceDTO.class
                        ))
                .toList();
    }

    // Update attendance
    public AttendanceDTO updateAttendance(
            String id,
            AttendanceDTO attendanceDTO) {
        studentAccessService.requireAcademicWriteAccess(attendanceDTO.getStudentId());
        validateStatus(attendanceDTO.getStatus());

        Attendance existingAttendance =
                attendanceRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Attendance not found with id : "
                                                + id
                                ));
        studentAccessService.requireAcademicWriteAccess(existingAttendance.getStudentId());

        // Check student exists
        Student student = studentRepository.findById(
                attendanceDTO.getStudentId()
        ).orElseThrow(() ->
                new ResourceNotFoundException(
                        "Student not found with id : "
                                + attendanceDTO.getStudentId()
                )
        );

        // Check student has batch
        if (student.getBatchId() == null ||
                student.getBatchId().isBlank()) {

            throw new ResourceNotFoundException(
                    "Student is not assigned to any batch"
            );
        }

        // Check duplicate date/student combination
        if (!existingAttendance.getStudentId()
                .equals(attendanceDTO.getStudentId())
                || !existingAttendance.getDate()
                .equals(attendanceDTO.getDate())) {

            if (attendanceRepository.existsByStudentIdAndDate(
                    attendanceDTO.getStudentId(),
                    attendanceDTO.getDate())) {

                throw new DuplicateResourceException(
                        "Attendance already exists for this student on "
                                + attendanceDTO.getDate()
                );
            }
        }

        // DTO -> Entity
        Attendance attendance =
                modelMapper.map(
                        attendanceDTO,
                        Attendance.class
                );

        // Preserve existing ID
        attendance.setId(existingAttendance.getId());

        // Save
        Attendance updatedAttendance =
                attendanceRepository.save(attendance);
        if (!existingAttendance.getStudentId().equals(updatedAttendance.getStudentId())) {
            recordCourseAttendance("ATTENDANCE_REASSIGNED", existingAttendance.getStudentId(), existingAttendance.getStatus());
        }
        recordCourseAttendance("ATTENDANCE_UPDATED", updatedAttendance.getStudentId(), updatedAttendance.getStatus());

        // Entity -> DTO
        return modelMapper.map(
                updatedAttendance,
                AttendanceDTO.class
        );
    }

    // Delete attendance
    public void deleteAttendance(String id) {
        studentAccessService.requireAdministrator();

        Attendance attendance =
                attendanceRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Attendance not found with id : "
                                                + id
                                ));

        attendanceRepository.delete(attendance);
                recordCourseAttendance("ATTENDANCE_DELETED", attendance.getStudentId(), attendance.getStatus());
    }

        private void recordCourseAttendance(String action, String studentId, String status) {
                studentRepository.findById(studentId)
                                .map(Student::getBatchId)
                                .filter(batchId -> batchId != null && !batchId.isBlank())
                                .flatMap(batchRepository::findById)
                                .map(Batch::getCourseId)
                                .ifPresent(courseId -> {
                                        Course course = courseRepository.findById(courseId).orElse(null);
                                        courseAuditService.record(action, courseId,
                                                        course == null ? null : course.getCourseName(),
                                                        "Attendance status " + status + " for student " + studentId);
                                });
        }

        private void validateStatus(String status) {
                if (status == null || !List.of("PRESENT", "ABSENT", "LATE", "EXCUSED")
                                .contains(status.toUpperCase())) {
                        throw new com.edutrack.studentportal.exception.InvalidRequestException(
                                        "Attendance status must be PRESENT, ABSENT, LATE, or EXCUSED");
                }
        }
}