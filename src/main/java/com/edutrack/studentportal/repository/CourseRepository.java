package com.edutrack.studentportal.repository;

import com.edutrack.studentportal.model.Course;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CourseRepository extends MongoRepository<Course, String> {

    List<Course> findByCourseNameContainingIgnoreCase(String courseName);

}
