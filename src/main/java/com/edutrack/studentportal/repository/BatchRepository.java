package com.edutrack.studentportal.repository;

import com.edutrack.studentportal.model.Batch;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BatchRepository extends MongoRepository<Batch, String> {

    //search batch by batchname
    List<Batch> findByBatchNameContainingIgnoreCase(String batchName);

    //get all batches of a course
    List<Batch> findByCourseId(String courseId);
}
