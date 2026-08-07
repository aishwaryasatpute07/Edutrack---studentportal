package com.edutrack.studentportal.repository;

import com.edutrack.studentportal.model.Student;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends MongoRepository<Student, String> {

    List<Student> findByNameContainingIgnoreCase(String name);

    @Query("{'email': ?0}")
    Optional<Student> findStudentByEmail(String email);


}