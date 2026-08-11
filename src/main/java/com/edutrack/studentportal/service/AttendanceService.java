package com.edutrack.studentportal.service;

import com.edutrack.studentportal.dto.AttendanceDTO;
import com.edutrack.studentportal.exception.DuplicateResourceException;
import com.edutrack.studentportal.exception.ResourceNotFoundException;
import com.edutrack.studentportal.model.Attendance;
import com.edutrack.studentportal.model.Student;
import com.edutrack.studentportal.repository.AttendanceRepository;
import com.edutrack.studentportal.repository.StudentRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final ModelMapper modelMapper;

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            StudentRepository studentRepository,
            ModelMapper modelMapper) {

        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.modelMapper = modelMapper;
    }

    // Create attendance
    public AttendanceDTO saveAttendance(AttendanceDTO attendanceDTO) {

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

        // 6. Entity -> DTO
        return modelMapper.map(
                savedAttendance,
                AttendanceDTO.class
        );
    }

    // Get all attendance
    public List<AttendanceDTO> getAllAttendance() {

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

        Attendance existingAttendance =
                attendanceRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Attendance not found with id : "
                                                + id
                                ));

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

        // Entity -> DTO
        return modelMapper.map(
                updatedAttendance,
                AttendanceDTO.class
        );
    }

    // Delete attendance
    public void deleteAttendance(String id) {

        Attendance attendance =
                attendanceRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Attendance not found with id : "
                                                + id
                                ));

        attendanceRepository.delete(attendance);
    }
}