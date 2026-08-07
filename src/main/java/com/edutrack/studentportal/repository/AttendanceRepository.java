package com.edutrack.studentportal.repository;

import com.edutrack.studentportal.model.Attendance;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AttendanceRepository extends MongoRepository<Attendance, String> {
}
