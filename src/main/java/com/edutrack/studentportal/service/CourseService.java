package com.edutrack.studentportal.service;

import com.edutrack.studentportal.dto.CourseDTO;
import com.edutrack.studentportal.dto.CourseHierarchyDTO;
import com.edutrack.studentportal.exception.DuplicateResourceException;
import com.edutrack.studentportal.exception.InvalidRequestException;
import com.edutrack.studentportal.exception.ResourceNotFoundException;
import com.edutrack.studentportal.model.Course;
import com.edutrack.studentportal.model.CourseAuditLog;
import com.edutrack.studentportal.repository.CourseRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final ModelMapper modelMapper;
    private final CourseAuditService courseAuditService;

    public CourseService(CourseRepository courseRepository, ModelMapper modelMapper,
                         CourseAuditService courseAuditService) {
        this.courseRepository = courseRepository;
        this.modelMapper = modelMapper;
        this.courseAuditService = courseAuditService;
    }

    public CourseDTO saveCourse(CourseDTO courseDTO) {

        courseDTO.setParentCourseId(normalizeParentId(courseDTO.getParentCourseId()));
        validateParentCourse(courseDTO.getParentCourseId(), null);

        if (courseDTO.getStatus() == null || courseDTO.getStatus().isBlank()) {
            courseDTO.setStatus("DRAFT");
        }

        if (courseRepository.existsByCourseNameAndDurationAndFeesAndDescription(
                courseDTO.getCourseName(),
                courseDTO.getDuration(),
                courseDTO.getFees(),
                courseDTO.getDescription()
        )) {
            throw new DuplicateResourceException(
                    "Course already exists with the same details"
            );
        }

        Course course = modelMapper.map(courseDTO, Course.class);

        if (course.getEnrolledStudents() == null) {
            course.setEnrolledStudents(0);
        }

        Course savedCourse = courseRepository.save(course);
        courseAuditService.record("COURSE_CREATED", savedCourse.getId(), savedCourse.getCourseName(),
                "Course created with status " + savedCourse.getStatus());

        return modelMapper.map(savedCourse, CourseDTO.class);
    }

    public List<CourseHierarchyDTO> getCourseHierarchy() {
        List<Course> courses = courseRepository.findAll(Sort.by("courseName").ascending());
        Map<String, CourseHierarchyDTO> nodes = new HashMap<>();
        Map<String, List<CourseHierarchyDTO>> childrenByParent = new HashMap<>();

        for (Course course : courses) {
            CourseHierarchyDTO node = new CourseHierarchyDTO();
            node.setId(course.getId());
            node.setCourseName(course.getCourseName());
            node.setStatus(course.getStatus());
            node.setInstructorName(course.getInstructorName());
            node.setParentCourseId(course.getParentCourseId());
            nodes.put(course.getId(), node);
        }

        List<CourseHierarchyDTO> roots = new ArrayList<>();
        for (Course course : courses) {
            CourseHierarchyDTO node = nodes.get(course.getId());
            String parentId = course.getParentCourseId();
            if (parentId == null || parentId.isBlank() || !nodes.containsKey(parentId)) {
                roots.add(node);
            } else {
                childrenByParent.computeIfAbsent(parentId, ignored -> new ArrayList<>()).add(node);
            }
        }

        Set<String> attached = new HashSet<>();
        roots.forEach(root -> {
            attached.add(root.getId());
            attachChildren(root, childrenByParent, attached, new HashSet<>());
        });
        for (Course course : courses) {
            if (!attached.contains(course.getId())) {
                CourseHierarchyDTO node = nodes.get(course.getId());
                roots.add(node);
                attached.add(node.getId());
                attachChildren(node, childrenByParent, attached, new HashSet<>());
            }
        }
        return roots;
    }

    private void attachChildren(CourseHierarchyDTO parent,
                                Map<String, List<CourseHierarchyDTO>> childrenByParent,
                                Set<String> attached,
                                Set<String> path) {
        if (!path.add(parent.getId())) {
            return;
        }
        List<CourseHierarchyDTO> children = childrenByParent.getOrDefault(parent.getId(), List.of());
        List<CourseHierarchyDTO> safeChildren = children.stream()
            .filter(child -> !path.contains(child.getId()))
            .toList();
        parent.setChildren(safeChildren);
        for (CourseHierarchyDTO child : safeChildren) {
            attached.add(child.getId());
            attachChildren(child, childrenByParent, attached, new HashSet<>(path));
        }
    }

    private void validateParentCourse(String parentCourseId, String currentCourseId) {
        if (parentCourseId == null || parentCourseId.isBlank()) {
            return;
        }
        String parentId = parentCourseId.trim();
        Set<String> visited = new HashSet<>();
        while (parentId != null && !parentId.isBlank()) {
            if (parentId.equals(currentCourseId)) {
                throw new InvalidRequestException("A course cannot be its own ancestor");
            }
            if (!visited.add(parentId)) {
                throw new InvalidRequestException("Course hierarchy contains a cycle");
            }
                String lookupParentId = parentId;
                Course parent = courseRepository.findById(lookupParentId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                        "Parent course not found with id : " + lookupParentId));
            parentId = parent.getParentCourseId();
        }
    }

    private String normalizeParentId(String parentCourseId) {
        return parentCourseId == null || parentCourseId.isBlank() ? null : parentCourseId.trim();
    }

    public List<CourseDTO> getAllCourses() {
        List<Course> courses = courseRepository.findAll();

        return courses.stream()
                .map(course -> modelMapper.map(course, CourseDTO.class))
                .toList();
    }

    public List<CourseDTO> getCourses(String status, String instructorName) {
        if (status == null || status.isBlank()) {
            if (instructorName == null || instructorName.isBlank()) {
                return getAllCourses();
            }
            return courseRepository.findByInstructorNameContainingIgnoreCase(instructorName)
                    .stream()
                    .map(course -> modelMapper.map(course, CourseDTO.class))
                    .toList();
        }

        if (instructorName == null || instructorName.isBlank()) {
            return courseRepository.findByStatusIgnoreCase(status)
                    .stream()
                    .map(course -> modelMapper.map(course, CourseDTO.class))
                    .toList();
        }

        return courseRepository.findByStatusIgnoreCaseAndInstructorNameContainingIgnoreCase(status, instructorName)
                .stream()
                .map(course -> modelMapper.map(course, CourseDTO.class))
                .toList();
    }

    public CourseDTO getCourseById(String id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("course not found with id : " + id));

        return modelMapper.map(course, CourseDTO.class);
    }

    public List<CourseDTO> searchCourses(String courseName) {
        return courseRepository
                .findByCourseNameContainingIgnoreCase(courseName)
                .stream()
                .map(course -> modelMapper.map(course, CourseDTO.class))
                .toList();
    }

    public List<CourseDTO> getCoursesSortedByName() {
        List<Course> courses = courseRepository.findAll(
                Sort.by("courseName").ascending()
        );

        return courses.stream()
                .map(course -> modelMapper.map(course, CourseDTO.class))
                .toList();
    }

    public Page<CourseDTO> getAllCoursesWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Course> courses = courseRepository.findAll(pageable);

        return courses.map(course -> modelMapper.map(course, CourseDTO.class));
    }

    public CourseDTO updateCourse(String id, CourseDTO courseDTO) {
        Course existingCourse = courseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "course is not found with : " + id));

                courseDTO.setParentCourseId(normalizeParentId(courseDTO.getParentCourseId()));
                validateParentCourse(courseDTO.getParentCourseId(), id);

        Course course = modelMapper.map(courseDTO, Course.class);
        course.setId(existingCourse.getId());

        if (course.getEnrolledStudents() == null) {
            course.setEnrolledStudents(existingCourse.getEnrolledStudents());
        }

        if (course.getStatus() == null || course.getStatus().isBlank()) {
            course.setStatus(existingCourse.getStatus());
        }
        if (courseDTO.getDescriptionTranslations().isEmpty()) {
            course.setDescriptionTranslations(existingCourse.getDescriptionTranslations());
        }

        Course updatedCourse = courseRepository.save(course);
        courseAuditService.record("COURSE_UPDATED", updatedCourse.getId(), updatedCourse.getCourseName(),
                "Course details updated");

        return modelMapper.map(updatedCourse, CourseDTO.class);
    }

    public CourseDTO assignInstructor(String courseId, String instructorName) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id : " + courseId));

        if (instructorName == null || instructorName.isBlank()) {
            throw new IllegalArgumentException("Instructor name is required");
        }

        course.setInstructorName(instructorName);
        Course updatedCourse = courseRepository.save(course);
        courseAuditService.record("INSTRUCTOR_ASSIGNED", updatedCourse.getId(), updatedCourse.getCourseName(),
            "Instructor assigned: " + instructorName);

        return modelMapper.map(updatedCourse, CourseDTO.class);
    }

    public void deleteCourse(String id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course not found with id : " + id
                ));

        if (!courseRepository.findByParentCourseId(id).isEmpty()) {
            throw new InvalidRequestException("Delete or reassign child courses before deleting this course");
        }

        courseRepository.delete(course);
        courseAuditService.record("COURSE_DELETED", course.getId(), course.getCourseName(),
                "Course deleted");
    }

    public List<CourseAuditLog> getCourseAuditHistory(String courseId) {
        return courseAuditService.getCourseHistory(courseId);
    }
}
