package com.edutrack.studentportal.controller;

import com.edutrack.studentportal.dto.StudentDTO;
import com.edutrack.studentportal.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService){
        this.studentService = studentService;
    }

    //save student
    @PostMapping
    public StudentDTO addStudent(@Valid @RequestBody StudentDTO studentDTO){
        return studentService.saveStudent(studentDTO);
    }

    //get all students
    @GetMapping
    public List<StudentDTO> getAllStudents(){
        return studentService.getAllStudents();
    }

    // get student by id
    @GetMapping("/{id}")
    public StudentDTO getStudentById(@PathVariable String id){
        return studentService.getStudentById(id);
    }

    // search student
    @GetMapping("/search")
    public List<StudentDTO> searchStudents(@RequestParam String name){
        return studentService.searchStudents(name);
    }

    //get student in sorted
    @GetMapping("/sort/name")
    public List<StudentDTO> getStudentsSortedByName(){
        return studentService.getStudentsSortedByName();
    }

    //student with PAGINATION
    @GetMapping("/pagination")
    public Page<StudentDTO> getAllStudentsWithPagination(
            @RequestParam int page,
            @RequestParam int size
    ){
        return studentService.getAllStudentsWithPagination(page, size);
    }


    //update students
    @PutMapping("/{id}")
    public StudentDTO updateStudent(@PathVariable String id, @Valid @RequestBody StudentDTO studentDTO){
        return studentService.updateStudent(id,studentDTO);
    }

    //delete student
    @DeleteMapping("/{id}")
    public String deleteStudent(@PathVariable String id){
        studentService.deleteStudent(id);
        return "student deleted successfully";
    }


    //assign batch to a student
    @PutMapping("/{studentId}/batch/{batchId}")
    public StudentDTO assignBatchToStudent(
            @PathVariable("studentId") String id,
            @PathVariable("batchId") String batchId){

        return studentService.assignBatchToStudent(id, batchId);
    }
}