package com.edutrack.studentportal.repository;

import com.edutrack.studentportal.model.HallTicket;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface HallTicketRepository extends MongoRepository<HallTicket, String> {

    List<HallTicket> findByStudentIdOrderByExamDateAscStartTimeAsc(String studentId);

    boolean existsByCourseIdAndExaminationIgnoreCaseAndExamDateAndSeatNumberIgnoreCaseAndIdNot(
            String courseId, String examination, LocalDate examDate, String seatNumber, String id);
}
