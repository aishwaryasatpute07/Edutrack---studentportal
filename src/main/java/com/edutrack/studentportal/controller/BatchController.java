package com.edutrack.studentportal.controller;

import com.edutrack.studentportal.dto.BatchDTO;
import com.edutrack.studentportal.service.BatchService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/batches")
public class BatchController {

    public BatchService batchService;

    public BatchController(BatchService batchService){
        this.batchService = batchService;
    }

    @PostMapping
    public BatchDTO savedBatch(@Valid @RequestBody BatchDTO batchDTO){
        return batchService.saveBatch(batchDTO);
    }

    @GetMapping
    public List<BatchDTO> getAllBatches(){
        return batchService.getAllBatches();
    }

    @GetMapping("/{id}")
    public BatchDTO getBatchById(@PathVariable String id){
        return batchService.getBatchById(id);
    }

    //search batch by name
    @GetMapping("/search")
    public List<BatchDTO> searchBatches(@RequestParam String batchName){
        return batchService.searchBatch(batchName);
    }

    //get batches in sorted
    @GetMapping("/sort/name")
    public List<BatchDTO> getBatchesSortedByName(){
        return batchService.getBatchesSortedByName();
    }

    //pagination
    @GetMapping("/pagination")
    public Page<BatchDTO> getAllBatchesWithPagination(@RequestParam int page, @RequestParam int size){
        return batchService.getAllBatchesWithPagination(page,size);
    }

    //get batch by courseId
    @GetMapping("/course/{courseId}")
    public List<BatchDTO> getBatchesByCourseId(@PathVariable String courseId){
        return batchService.getBatchesByCourseId(courseId);
    }

    //update batch
    @PutMapping("/{id}")
    public BatchDTO updateBatch(@PathVariable String id,
                                @Valid @RequestBody BatchDTO batchDTO){

        return batchService.updateBatch(id,batchDTO);
    }

    //delete batch
    @DeleteMapping("/{id}")
    public String deleteBatch(@PathVariable String id){

        batchService.deleteBatch(id);

        return "Batch deleted successfully";
    }
}