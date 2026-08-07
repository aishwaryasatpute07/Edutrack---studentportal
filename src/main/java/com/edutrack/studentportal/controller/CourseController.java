package com.edutrack.studentportal.controller;

import com.edutrack.studentportal.dto.CourseDTO;
import com.edutrack.studentportal.model.Course;
import com.edutrack.studentportal.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/courses")
public class CourseController {

    public CourseService courseService;

    public CourseController(CourseService courseService){
        this.courseService = courseService;
    }

    @PostMapping
    public CourseDTO saveCourse(@Valid @RequestBody CourseDTO courseDTO){
        return courseService.saveCourse(courseDTO);
    }

    @GetMapping
    public List<CourseDTO> getAllCourses(){
        return courseService.getAllCourses();
    }

    @GetMapping("/{id}")
    public CourseDTO getCourseById(@PathVariable String id){
        return courseService.getCourseById(id);
    }

    //search courses by name
    @GetMapping("/search")
    public List<CourseDTO> searchCourseByName(@RequestParam String courseName){
        return courseService.searchCourses(courseName);
    }

    //sorted courses by name
    @GetMapping("/sort/name")
    public List<CourseDTO> getCoursesSortedByName(){
        return courseService.getCoursesSortedByName();
    }

    //Pagination
    @GetMapping("/pagination")
    public Page<CourseDTO> getAllCoursesWithPagination(@RequestParam int page,
                                                       @RequestParam int size){
        return courseService.getAllCoursesWithPagination(page, size);
    }

    @PutMapping("/{id}")
    public CourseDTO updateCourse(@PathVariable String id,
                                  @Valid @RequestBody CourseDTO courseDTO){
        return courseService.updateCourse(id, courseDTO);
    }

    @DeleteMapping("/{id}")
    public String deleteCourse(@PathVariable String id){
        courseService.deleteCourse(id);
        return "course details deleted successfully";
    }
}
