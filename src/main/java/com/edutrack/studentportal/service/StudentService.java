package com.edutrack.studentportal.service;

import com.edutrack.studentportal.dto.StudentDTO;
import com.edutrack.studentportal.exception.DuplicateResourceException;
import com.edutrack.studentportal.exception.ResourceNotFoundException;
import com.edutrack.studentportal.model.Batch;
import com.edutrack.studentportal.model.Student;
import com.edutrack.studentportal.model.User;
import com.edutrack.studentportal.repository.BatchRepository;
import com.edutrack.studentportal.repository.CourseRepository;
import com.edutrack.studentportal.repository.StudentRepository;
import com.edutrack.studentportal.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final BatchRepository batchRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
        private final CourseRepository courseRepository;
        private final CourseAuditService courseAuditService;

    //Constructor injection
    public StudentService(StudentRepository studentRepository,
                          BatchRepository batchRepository,
                          ModelMapper modelMapper,
                          UserRepository userRepository,
                          CourseRepository courseRepository,
                          CourseAuditService courseAuditService) {

        this.studentRepository = studentRepository;
        this.batchRepository = batchRepository;
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.courseAuditService = courseAuditService;
    }

    //create student
    public StudentDTO saveStudent(StudentDTO studentDTO) {

        //Check duplicate email
        if (studentRepository.existsByEmail(
                studentDTO.getEmail())) {
            throw new DuplicateResourceException(
                    "Student already exists with email : " + studentDTO.getEmail()
            );
        }

        //Check duplicate mobile
        if (studentRepository.existsByMobile(
                studentDTO.getMobile())) {
            throw new DuplicateResourceException(
                    "Student already exists with mobile : " + studentDTO.getMobile()
            );
        }

        //DTO -> Entity
        Student student = modelMapper.map(studentDTO, Student.class);
        student.setLastUpdatedAt(LocalDateTime.now());

        //If batchId is provided, check batch
        if (studentDTO.getBatchId() != null &&
                !studentDTO.getBatchId().isBlank()) {

            //Check batch exists
            Batch batch = batchRepository.findById(studentDTO.getBatchId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Batch not found with id : "
                                            + studentDTO.getBatchId()
                            ));

            //Count students already assigned to batch
            long currentStudents =
                    studentRepository.countByBatchId(studentDTO.getBatchId());

            //Check capacity
            if (currentStudents >= batch.getCapacity()) {
                throw new RuntimeException("Batch is full");
            }
        }

        //Save student
        Student savedStudent = studentRepository.save(student);
        recordStudentBatchAction("STUDENT_ADDED_TO_COURSE_BATCH", savedStudent.getBatchId(),
                "Student added to cohort: " + savedStudent.getId());

        //Entity -> DTO
        return modelMapper.map(savedStudent, StudentDTO.class);
    }

    //get All students
    public List<StudentDTO> getAllStudents(){

        //store the all students
        List<Student> students = studentRepository.findAll();

        //create new arraylist
        List<StudentDTO> studentDTOS = new ArrayList<>();

        //store students from Student to StudentDTO
        for(Student student : students){

            studentDTOS.add(modelMapper.map(student,StudentDTO.class));
        }

        return studentDTOS;
    }

    //get Student By id
    public StudentDTO getStudentById(String id){
        Student student = studentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("student not found with id : "+id));

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String username = authentication.getName();

        //ADMIN can view any student
        if(authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"))){

            return toStudentDTO(student);
        }

        //get logged-in students user record
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        ));

        //student can view only their own record
        if(!id.equals(user.getStudentId())){

            throw new AccessDeniedException(
                    "yor are not allowed to access this student"
            );
        }

        return toStudentDTO(student);
    }

    //search student by name
    public List<StudentDTO> searchStudents(String name){

        return studentRepository
                .findByNameContainingIgnoreCase(name)
                .stream()
                .map(student -> modelMapper.map(student, StudentDTO.class))
                .toList();
    }

    //students by sorted name
    public List<StudentDTO> getStudentsSortedByName(){

        List<Student> students = studentRepository.findAll(
                Sort.by("name").ascending()
        );

        return students
                .stream()
                .map(this::toStudentDTO)
                .toList();
    }

    //pagination
    public Page<StudentDTO> getAllStudentsWithPagination(int page, int size){

        Pageable pageable = PageRequest.of(page, size);

        Page<Student> students = studentRepository.findAll(pageable);

        return students.map(this::toStudentDTO);
    }


    //Update student
    public StudentDTO updateStudent(String id, StudentDTO studentDTO) {

        Student existingStudent = studentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Student not found with id : " + id
                        ));

        //Check batch only if batchId is provided
        if (studentDTO.getBatchId() != null &&
                !studentDTO.getBatchId().isBlank()) {

            //Check batch exists
            Batch batch = batchRepository.findById(studentDTO.getBatchId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Batch not found with id : "
                                            + studentDTO.getBatchId()
                            ));

            //Only check capacity if student is moving to another batch
            if (!studentDTO.getBatchId().equals(existingStudent.getBatchId())) {

                long currentStudents =
                        studentRepository.countByBatchId(studentDTO.getBatchId());

                if (currentStudents >= batch.getCapacity()) {
                    throw new RuntimeException("Batch is full");
                }
            }
        }

        //DTO -> Entity
        Student student = modelMapper.map(studentDTO, Student.class);

        //Keep existing ID
        student.setId(existingStudent.getId());
        student.setLastUpdatedAt(LocalDateTime.now());

        //Save updated student
        Student updatedStudent = studentRepository.save(student);
        if (!java.util.Objects.equals(existingStudent.getBatchId(), updatedStudent.getBatchId())) {
            recordStudentBatchAction("STUDENT_COHORT_UPDATED", existingStudent.getBatchId(),
                    "Student removed from cohort: " + updatedStudent.getId());
            recordStudentBatchAction("STUDENT_COHORT_UPDATED", updatedStudent.getBatchId(),
                    "Student assigned to cohort: " + updatedStudent.getId());
        }

        //Return DTO
        return toStudentDTO(updatedStudent);
    }

    //delete student
    public void deleteStudent(String id){

        Student student = studentRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("student not found with id : "+id));

        studentRepository.deleteById(id);
        recordStudentBatchAction("STUDENT_REMOVED_FROM_COURSE_BATCH", student.getBatchId(),
                "Student removed from cohort: " + student.getId());
    }

    //delete these methods because modelmapper replace these methods
    //DTO -> Entity
    /*
    private Student convertToEntity(StudentDTO studentDTO){

        Student student = new Student();

        student.setId(studentDTO.getId());
        student.setName(studentDTO.getName());
        student.setEmail(studentDTO.getEmail());

        return student;
    }
    */

    //Entity -> DTO
    /*
    private StudentDTO convertToDTO(Student student){

        StudentDTO studentDTO = new StudentDTO();

        studentDTO.setId(student.getId());
        studentDTO.setName(student.getName());
        studentDTO.setEmail(student.getEmail());

        return studentDTO;
    }
    */

    //Assign batchId to STUDENT
    public StudentDTO assignBatchToStudent(String id, String batchId) {

        //1. Check student exists
        Student student = studentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Student not found : " + id
                        ));

        //2. Check batch exists
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Batch not found : " + batchId
                        ));

        //3. If student is already in this batch
        if (batchId.equals(student.getBatchId())) {
            return toStudentDTO(student);
        }

        //4. Count students already assigned to this batch
        long currentStudents =
                studentRepository.countByBatchId(batchId);

        //5. Check batch capacity
        if (currentStudents >= batch.getCapacity()) {
            throw new RuntimeException("Batch is full");
        }

        //6. Assign batch
        String previousBatchId = student.getBatchId();
        student.setBatchId(batchId);
        student.setLastUpdatedAt(LocalDateTime.now());
        student.setLastUpdatedAt(LocalDateTime.now());

        //7. Save student
        Student updatedStudent = studentRepository.save(student);
        recordStudentBatchAction("STUDENT_COHORT_UPDATED", previousBatchId,
                "Student removed from cohort: " + student.getId());
        recordStudentBatchAction("STUDENT_COHORT_UPDATED", student.getBatchId(),
                "Student assigned to cohort: " + student.getId());

        //8. Return DTO
        return toStudentDTO(updatedStudent);
    }

    private StudentDTO toStudentDTO(Student student) {
        StudentDTO dto = modelMapper.map(student, StudentDTO.class);
        dto.setLastUpdatedAt(student.getLastUpdatedAt());
        if (student.getBatchId() != null) {
            batchRepository.findById(student.getBatchId()).ifPresent(batch -> {
                dto.setBatchName(batch.getBatchName());
                dto.setTrainerName(batch.getTrainerName());
                dto.setCourseId(batch.getCourseId());
                courseRepository.findById(batch.getCourseId()).ifPresent(course -> {
                    dto.setCourseName(course.getCourseName());
                    dto.setInstructorName(course.getInstructorName());
                });
            });
        }
        return dto;
    }

    public List<StudentDTO> getStudentsForCurrentInstructor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User instructor = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Instructor account not found"));
        if (!"INSTRUCTOR".equalsIgnoreCase(instructor.getRole())) {
            throw new AccessDeniedException("Instructor access is required");
        }
        List<String> batchIds = batchRepository.findByTrainerUsernameIgnoreCase(instructor.getUsername())
                .stream().map(Batch::getId).toList();
        if (batchIds.isEmpty()) {
            return List.of();
        }
        return studentRepository.findByBatchIdIn(batchIds).stream()
                .map(this::toStudentDTO)
                .toList();
    }

        private void recordStudentBatchAction(String action, String batchId, String details) {
                if (batchId == null || batchId.isBlank()) {
                        return;
                }
                batchRepository.findById(batchId).ifPresent(batch -> {
                        String courseId = batch.getCourseId();
                        String courseName = courseRepository.findById(courseId)
                                        .map(com.edutrack.studentportal.model.Course::getCourseName)
                                        .orElse(null);
                        courseAuditService.record(action, courseId, courseName, details);
                });
        }
}