package com.edutrack.studentportal;

import com.edutrack.studentportal.model.Course;
import com.edutrack.studentportal.model.Enrollment;
import com.edutrack.studentportal.dto.PerformanceSummaryDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CourseFeatureTest {

    @Test
    void shouldSupportCourseStatusInstructorAndEnrollmentState() {
        Course course = new Course();
        course.setCourseName("Spring Boot Mastery");
        course.setStatus("OPEN");
        course.setInstructorName("Jane Doe");

        Enrollment enrollment = new Enrollment();
        enrollment.setCourseId("course-123");
        enrollment.setStudentId("student-456");
        enrollment.setStatus("ENROLLED");

        PerformanceSummaryDTO summary = new PerformanceSummaryDTO();
        summary.setCourseId("course-123");
        summary.setAverageScore(88.0);

        assertEquals("OPEN", course.getStatus());
        assertEquals("Jane Doe", course.getInstructorName());
        assertEquals("ENROLLED", enrollment.getStatus());
        assertEquals(88.0, summary.getAverageScore());
    }
}
