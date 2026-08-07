package com.edutrack.studentportal.service;

import com.edutrack.studentportal.dto.StudentDTO;
import com.edutrack.studentportal.exception.ResourceNotFoundException;
import com.edutrack.studentportal.model.Batch;
import com.edutrack.studentportal.model.Student;
import com.edutrack.studentportal.repository.BatchRepository;
import com.edutrack.studentportal.repository.StudentRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    private final BatchRepository batchRepository;

    private final ModelMapper modelMapper;

    //Constructor injection
    public StudentService(StudentRepository studentRepository, BatchRepository batchRepository, ModelMapper modelMapper) {
        this.studentRepository = studentRepository;
        this.batchRepository = batchRepository;
        this.modelMapper = modelMapper;
    }

    //create student
    public StudentDTO saveStudent(StudentDTO studentDTO){

        //Convert DTO -> entity
        Student student = modelMapper.map(
                studentDTO,
                Student.class
        );

        //save entity
        Student saveStudent = studentRepository.save(student);

        //convert entity -> DTO
        return modelMapper.map(
                saveStudent,
                StudentDTO.class
        );
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

        return modelMapper.map(student, StudentDTO.class);
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
                .map(student -> modelMapper.map(student, StudentDTO.class))
                .toList();
    }

    //pagination
    public Page<StudentDTO> getAllStudentsWithPagination(int page, int size){

        Pageable pageable = PageRequest.of(page, size);

        Page<Student> students = studentRepository.findAll(pageable);

        return students
                .map(student -> modelMapper.map(student, StudentDTO.class));
    }


    //Update student
    public StudentDTO updateStudent(String id, StudentDTO studentDTO){

        Student existingStudent = studentRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("student not found with id : "+id));

        //convert DTO -> Entity
        Student student = modelMapper.map(studentDTO,Student.class);

        student.setId(existingStudent.getId());

        //save updated student
        Student updatedStudent = studentRepository.save(student);

        //return DTO not entity
        return modelMapper.map(updatedStudent,StudentDTO.class);
    }

    //delete student
    public void deleteStudent(String id){

        Student student = studentRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("student not found with id : "+id));

        studentRepository.deleteById(id);
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
    public StudentDTO assignBatchToStudent(String id, String batchId){

        //check students exists
        Student student = studentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("student not found : "+id));

        //check batch exists
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("batch not found "+batchId));

        //set batch id in a student
        student.setBatchId(batchId);

        //save updated student
        Student updatedStudent = studentRepository.save(student);

        //return to DTO
        return modelMapper.map(updatedStudent, StudentDTO.class);
    }
}