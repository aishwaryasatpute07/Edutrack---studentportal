package com.edutrack.studentportal;

import com.edutrack.studentportal.dto.CourseDTO;
import com.edutrack.studentportal.dto.CourseHierarchyDTO;
import com.edutrack.studentportal.exception.InvalidRequestException;
import com.edutrack.studentportal.model.Course;
import com.edutrack.studentportal.repository.CourseRepository;
import com.edutrack.studentportal.service.CourseAuditService;
import com.edutrack.studentportal.service.CourseService;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CourseHierarchyTest {

    private final CourseRepository courseRepository = mock(CourseRepository.class);
    private final CourseService courseService = new CourseService(
            courseRepository,
            mock(ModelMapper.class),
            mock(CourseAuditService.class));

    @Test
    void hierarchyShouldNestCoursesUnderTheirParent() {
        Course parent = course("parent", "Backend", null);
        Course child = course("child", "Spring", "parent");
        when(courseRepository.findAll(org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of(parent, child));

        List<CourseHierarchyDTO> hierarchy = courseService.getCourseHierarchy();

        assertEquals(1, hierarchy.size());
        assertEquals("parent", hierarchy.get(0).getId());
        assertEquals(1, hierarchy.get(0).getChildren().size());
        assertEquals("child", hierarchy.get(0).getChildren().get(0).getId());
    }

    @Test
    void updateShouldRejectSelfParenting() {
        Course existing = course("course-1", "Backend", null);
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(existing));
        CourseDTO update = new CourseDTO();
        update.setParentCourseId("course-1");

        assertThrows(InvalidRequestException.class,
                () -> courseService.updateCourse("course-1", update));
    }

    private Course course(String id, String name, String parentId) {
        Course course = new Course();
        course.setId(id);
        course.setCourseName(name);
        course.setParentCourseId(parentId);
        course.setStatus("OPEN");
        return course;
    }
}