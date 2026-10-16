package com.edutrack.studentportal.repository;

import com.edutrack.studentportal.model.CourseAuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CourseAuditLogRepository extends MongoRepository<CourseAuditLog, String> {

    List<CourseAuditLog> findByCourseIdOrderByOccurredAtDesc(String courseId);
}