package com.edutrack.studentportal.repository;

import com.edutrack.studentportal.model.Attendance;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository
        extends MongoRepository<Attendance, String> {

    // Get attendance of a student
    List<Attendance> findByStudentId(String studentId);

    // Get attendance of a student on a particular date
    boolean existsByStudentIdAndDate(
            String studentId,
            LocalDate date
    );

    // Get attendance by date
    List<Attendance> findByDate(LocalDate date);
}