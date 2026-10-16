package com.edutrack.studentportal.controller;

import com.edutrack.studentportal.dto.StudentDTO;
import com.edutrack.studentportal.service.StudentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/instructors")
public class InstructorController {

    private final StudentService studentService;

    public InstructorController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/me/students")
    public List<StudentDTO> getAssignedStudents() {
        return studentService.getStudentsForCurrentInstructor();
    }
}
