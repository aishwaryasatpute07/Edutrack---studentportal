package com.edutrack.studentportal.service;

import com.edutrack.studentportal.model.CourseAuditLog;
import com.edutrack.studentportal.repository.CourseAuditLogRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseAuditService {

    private final CourseAuditLogRepository auditLogRepository;

    public CourseAuditService(CourseAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void record(String action, String courseId, String courseName, String details) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String actor = authentication == null || !authentication.isAuthenticated()
                ? "system"
                : authentication.getName();

        CourseAuditLog log = new CourseAuditLog();
        log.setAction(action);
        log.setCourseId(courseId);
        log.setCourseName(courseName);
        log.setActor(actor);
        log.setDetails(details);
        log.setOccurredAt(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    public List<CourseAuditLog> getCourseHistory(String courseId) {
        return auditLogRepository.findByCourseIdOrderByOccurredAtDesc(courseId);
    }
}