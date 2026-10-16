package com.edutrack.studentportal.dto;

import java.util.ArrayList;
import java.util.List;

public class CourseHierarchyDTO {

    private String id;
    private String courseName;
    private String status;
    private String instructorName;
    private String parentCourseId;
    private List<CourseHierarchyDTO> children = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }

    public String getParentCourseId() {
        return parentCourseId;
    }

    public void setParentCourseId(String parentCourseId) {
        this.parentCourseId = parentCourseId;
    }

    public List<CourseHierarchyDTO> getChildren() {
        return children;
    }

    public void setChildren(List<CourseHierarchyDTO> children) {
        this.children = children;
    }
}