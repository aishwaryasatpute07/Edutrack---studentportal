package com.edutrack.studentportal.repository;

import com.edutrack.studentportal.model.Course;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CourseRepository extends MongoRepository<Course, String> {

    List<Course> findByCourseNameContainingIgnoreCase(String courseName);

    List<Course> findByStatusIgnoreCase(String status);

    List<Course> findByInstructorNameContainingIgnoreCase(String instructorName);

    List<Course> findByStatusIgnoreCaseAndInstructorNameContainingIgnoreCase(String status, String instructorName);

    List<Course> findByParentCourseId(String parentCourseId);

    boolean existsByCourseNameAndDurationAndFeesAndDescription(
            String courseName,
            String duration,
            double fees,
            String description
    );
}
