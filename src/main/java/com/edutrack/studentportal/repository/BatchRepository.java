package com.edutrack.studentportal.repository;

import com.edutrack.studentportal.model.Batch;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface BatchRepository extends MongoRepository<Batch, String> {

    //search batch by batchname
    List<Batch> findByBatchNameContainingIgnoreCase(String batchName);

    //get all batches of a course
    List<Batch> findByCourseId(String courseId);

    //Check exact duplicate batch data except ID
    boolean existsByBatchNameAndCourseIdAndTrainerNameAndStartDateAndEndDateAndCapacityAndStatus(
            String batchName,
            String courseId,
            String trainerName,
            LocalDate startDate,
            LocalDate endDate,
            Integer capacity,
            String status );
}
