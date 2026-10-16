package com.edutrack.studentportal.repository;

import com.edutrack.studentportal.model.AcademicRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AcademicRecordRepository extends MongoRepository<AcademicRecord, String> {
    List<AcademicRecord> findByStudentIdOrderByUpdatedAtDesc(String studentId);

    List<AcademicRecord> findByCourseId(String courseId);

    List<AcademicRecord> findByStudentId(String studentId);

    boolean existsByStudentIdAndCourseIdAndSubjectIdAndExaminationAndAttemptNumber(
            String studentId, String courseId, String subjectId, String examination, int attemptNumber);

    boolean existsByStudentIdAndCourseIdAndSubjectIdAndExaminationAndAttemptNumberAndIdNot(
            String studentId, String courseId, String subjectId, String examination, int attemptNumber, String id);
}
