package com.edutrack.studentportal.controller;

import com.edutrack.studentportal.dto.CourseDTO;
import com.edutrack.studentportal.dto.CourseHierarchyDTO;
import com.edutrack.studentportal.model.CourseAuditLog;
import com.edutrack.studentportal.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    public CourseDTO saveCourse(@Valid @RequestBody CourseDTO courseDTO) {
        return courseService.saveCourse(courseDTO);
    }

    @GetMapping
    public List<CourseDTO> getCourses(
            @RequestParam(required = false) String status,
            @RequestParam(required = false, name = "instructor") String instructorName) {
        return courseService.getCourses(status, instructorName);
    }

    @GetMapping("/hierarchy")
    public List<CourseHierarchyDTO> getCourseHierarchy() {
        return courseService.getCourseHierarchy();
    }

    @GetMapping("/{id}/audit")
    public List<CourseAuditLog> getCourseAuditHistory(@PathVariable String id) {
        return courseService.getCourseAuditHistory(id);
    }

    @GetMapping("/{id}")
    public CourseDTO getCourseById(@PathVariable String id) {
        return courseService.getCourseById(id);
    }

    @GetMapping("/search")
    public List<CourseDTO> searchCourseByName(@RequestParam String courseName) {
        return courseService.searchCourses(courseName);
    }

    @GetMapping("/sort/name")
    public List<CourseDTO> getCoursesSortedByName() {
        return courseService.getCoursesSortedByName();
    }

    @GetMapping("/pagination")
    public Page<CourseDTO> getAllCoursesWithPagination(@RequestParam int page,
                                                       @RequestParam int size) {
        return courseService.getAllCoursesWithPagination(page, size);
    }

    @PutMapping("/{id}")
    public CourseDTO updateCourse(@PathVariable String id,
                                  @Valid @RequestBody CourseDTO courseDTO) {
        return courseService.updateCourse(id, courseDTO);
    }

    @PutMapping("/{id}/instructor")
    public CourseDTO assignInstructor(@PathVariable String id,
                                      @RequestParam String instructorName) {
        return courseService.assignInstructor(id, instructorName);
    }

    @DeleteMapping("/{id}")
    public String deleteCourse(@PathVariable String id) {
        courseService.deleteCourse(id);
        return "course details deleted successfully";
    }
}
