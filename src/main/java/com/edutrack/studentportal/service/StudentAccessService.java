package com.edutrack.studentportal.service;

import com.edutrack.studentportal.exception.ResourceNotFoundException;
import com.edutrack.studentportal.model.Batch;
import com.edutrack.studentportal.model.Student;
import com.edutrack.studentportal.model.User;
import com.edutrack.studentportal.repository.BatchRepository;
import com.edutrack.studentportal.repository.StudentRepository;
import com.edutrack.studentportal.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class StudentAccessService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final BatchRepository batchRepository;

    public StudentAccessService(UserRepository userRepository,
                                StudentRepository studentRepository,
                                BatchRepository batchRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.batchRepository = batchRepository;
    }

    public User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("Authentication is required");
        }
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Authenticated account was not found"));
    }

    public String requireCurrentStudentId() {
        User user = currentUser();
        if (!"STUDENT".equalsIgnoreCase(user.getRole())
                || user.getStudentId() == null || user.getStudentId().isBlank()) {
            throw new AccessDeniedException("A linked student account is required");
        }
        return user.getStudentId();
    }

    public Student requireStudentAccess(String studentId) {
        User user = currentUser();
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return findStudent(studentId);
        }
        if ("STUDENT".equalsIgnoreCase(user.getRole())
                && studentId.equals(user.getStudentId())) {
            return findStudent(studentId);
        }
        if ("INSTRUCTOR".equalsIgnoreCase(user.getRole()) && instructorAssigned(user, studentId)) {
            return findStudent(studentId);
        }
        throw new AccessDeniedException("You are not allowed to access this student");
    }

    public Student requireAcademicWriteAccess(String studentId) {
        User user = currentUser();
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            return findStudent(studentId);
        }
        if ("INSTRUCTOR".equalsIgnoreCase(user.getRole()) && instructorAssigned(user, studentId)) {
            return findStudent(studentId);
        }
        throw new AccessDeniedException("Only an administrator or the assigned instructor can update academic records");
    }

    public Student requireStudentOrAdminAccess(String studentId) {
        User user = currentUser();
        if ("ADMIN".equalsIgnoreCase(user.getRole())
                || ("STUDENT".equalsIgnoreCase(user.getRole())
                && studentId.equals(user.getStudentId()))) {
            return findStudent(studentId);
        }
        throw new AccessDeniedException("You are not allowed to access this student's account data");
    }

    public void requireAdministrator() {
        if (!"ADMIN".equalsIgnoreCase(currentUser().getRole())) {
            throw new AccessDeniedException("Administrator access is required");
        }
    }

    private Student findStudent(String studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
    }

    private boolean instructorAssigned(User user, String studentId) {
        Student student = studentRepository.findById(studentId).orElse(null);
        if (student == null || student.getBatchId() == null || student.getBatchId().isBlank()) {
            return false;
        }
        Batch batch = batchRepository.findById(student.getBatchId()).orElse(null);
        return batch != null && user.getUsername().equalsIgnoreCase(batch.getTrainerUsername());
    }
}
