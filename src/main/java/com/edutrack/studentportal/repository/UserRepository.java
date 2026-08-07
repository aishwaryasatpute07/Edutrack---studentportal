package com.edutrack.studentportal.repository;

import com.edutrack.studentportal.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String>{

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByStudentId(String studentId);

    List<User> findByUsernameContainingIgnoreCase(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
