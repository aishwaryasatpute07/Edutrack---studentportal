package com.edutrack.studentportal.service;

import com.edutrack.studentportal.dto.CourseDTO;
import com.edutrack.studentportal.exception.ResourceNotFoundException;
import com.edutrack.studentportal.model.Course;
import com.edutrack.studentportal.repository.CourseRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final ModelMapper modelMapper;

    public CourseService(CourseRepository courseRepository, ModelMapper modelMapper){
        this.courseRepository = courseRepository;
        this.modelMapper = modelMapper;
    }

    //create student
    public CourseDTO saveCourse(CourseDTO courseDTO){

        //DTO -> Entity
        Course course = modelMapper.map(courseDTO, Course.class);

        //save
        Course savedCourse = courseRepository.save(course);

        //Entity -> DTO
        return modelMapper.map(savedCourse, CourseDTO.class);
    }

    //get all courses
    public List<CourseDTO> getAllCourses(){
        //Fetch records
        List<Course> courses = courseRepository.findAll();

        //Entity -> DTO
        return courses.stream()
                .map(course -> modelMapper.map(course, CourseDTO.class))
                .toList();
    }

    //get course by id
    public CourseDTO getCourseById(String id){

        //Fetch records
        Course course = courseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("course not found with id : "+id));

        //entity -> DTO
        return modelMapper.map(course, CourseDTO.class);
    }

    //get course by name
    public List<CourseDTO> searchCourses(String courseName){

        return courseRepository
                .findByCourseNameContainingIgnoreCase(courseName)
                .stream()
                .map(course -> modelMapper.map(course, CourseDTO.class))
                .toList();
    }

    //get courses sorted by name
    public List<CourseDTO> getCoursesSortedByName() {

        List<Course> courses = courseRepository.findAll(
                Sort.by("courseName").ascending()
        );

        return courses
                .stream()
                .map(course -> modelMapper.map(course, CourseDTO.class))
                .toList();
    }

    //pagination
    public Page<CourseDTO> getAllCoursesWithPagination(int page, int size){

        Pageable pageable = PageRequest.of(page, size);

        Page<Course> courses = courseRepository.findAll(pageable);

        return courses
                .map(course ->
                modelMapper.map(course, CourseDTO.class));
    }

    //update by id
    public CourseDTO updateCourse(String id, CourseDTO courseDTO){

        Course existingCourse = courseRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "course is not found with : "+id));

        //DTO -> Entity
        Course course = modelMapper.map(courseDTO, Course.class);

        //preserve existing id
        course.setId(existingCourse.getId());

        //save updated courese
        Course updatedCourse = courseRepository.save(course);

        //Entity -> DTO
        return modelMapper.map(updatedCourse, CourseDTO.class);
    }

    //delete course
    public void deleteCourse(String id) {

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        courseRepository.delete(course);
    }
}
