package com.edutrack.studentportal.service;

import com.edutrack.studentportal.dto.BatchDTO;
import com.edutrack.studentportal.exception.ResourceNotFoundException;
import com.edutrack.studentportal.exception.DuplicateResourceException;
import com.edutrack.studentportal.model.Batch;
import com.edutrack.studentportal.repository.BatchRepository;
import com.edutrack.studentportal.repository.CourseRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BatchService {

    private final BatchRepository batchRepository;
    private final ModelMapper modelMapper;
    private final CourseRepository courseRepository;

    public BatchService(BatchRepository batchRepository, ModelMapper modelMapper, CourseRepository courseRepository){
        this.batchRepository = batchRepository;
        this.modelMapper = modelMapper;
        this.courseRepository = courseRepository;
    }

    //create batch
    public BatchDTO saveBatch(BatchDTO batchDTO) {

        //Check if exact same batch already exists
        boolean duplicateBatch = batchRepository .existsByBatchNameAndCourseIdAndTrainerNameAndStartDateAndEndDateAndCapacityAndStatus(
                batchDTO.getBatchName(),
                batchDTO.getCourseId(),
                batchDTO.getTrainerName(),
                batchDTO.getStartDate(),
                batchDTO.getEndDate(),
                batchDTO.getCapacity(),
                batchDTO.getStatus()
        );

        if (duplicateBatch) {
            throw new DuplicateResourceException( "Same batch already exists" );
        }

        //Check course exists
        if (!courseRepository.existsById(batchDTO.getCourseId())) {
            throw new ResourceNotFoundException(
                    "Course not available with that id : "
                            + batchDTO.getCourseId()
            );
        }

        //DTO -> Entity
        Batch batch = modelMapper.map(batchDTO, Batch.class);

        //Save
        Batch savedBatch = batchRepository.save(batch);

        //Entity -> DTO
        return modelMapper.map(savedBatch, BatchDTO.class);
    }

    //get all batches
    public List<BatchDTO> getAllBatches(){

        //save all batches
        List<Batch> batches = batchRepository.findAll();

        //Entity -> DTO
        return batches
                .stream()
                .map(batch -> modelMapper.map(batch, BatchDTO.class))
                .toList();
    }

    // get batch by id
    public BatchDTO getBatchById(String id){

        //fetch records
        Batch batch = batchRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "not found batch with id : "+id));

        return modelMapper.map(batch, BatchDTO.class);
    }

    //search batch by name
    public List<BatchDTO> searchBatch(String batchName){

        return batchRepository
                .findByBatchNameContainingIgnoreCase(batchName)
                .stream()
                .map(batch -> modelMapper.map(batch, BatchDTO.class))
                .toList();
    }

    //Get batches sorted by name
    public List<BatchDTO> getBatchesSortedByName(){

        List<Batch> batches = batchRepository.findAll(
                Sort.by("batchName").ascending()
        );

        return batches
                .stream()
                .map(batch -> modelMapper.map(batch, BatchDTO.class))
                .toList();
    }

    //pagination
    public Page<BatchDTO> getAllBatchesWithPagination(int page, int size){

        Pageable pageable  = PageRequest.of(page, size);

        Page<Batch> batches = batchRepository.findAll(pageable);

        return batches.map(batch -> modelMapper.map(batch, BatchDTO.class));
    }

    //get batches by course id
    public List<BatchDTO> getBatchesByCourseId(String courseId){

        return batchRepository
                .findByCourseId(courseId)
                .stream()
                .map(batch -> modelMapper.map(batch, BatchDTO.class))
                .toList();
    }

    //update batch
    public BatchDTO updateBatch(String id, BatchDTO batchDTO){

        if (!courseRepository.existsById(batchDTO.getCourseId())) {
            throw new ResourceNotFoundException(
                    "Course not found with id : " + batchDTO.getCourseId()
            );
        }

        Batch existingBatch = batchRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "batch not found with id : "+id));

        //DTO -> Entity
        Batch batch = modelMapper.map(batchDTO, Batch.class);

        //keep existing id
        batch.setId(existingBatch.getId());

        //save updated batch
        Batch updatedBatch = batchRepository.save(batch);

        //Entity -> DTO
        return modelMapper.map(updatedBatch, BatchDTO.class);
    }

    //delete batch id
    public void deleteBatch(String id){

        Batch batch = batchRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "batch not found"));

        batchRepository.deleteById(id);
    }
}
