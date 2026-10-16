package com.edutrack.studentportal.service;

import com.edutrack.studentportal.dto.LoginDTO;
import com.edutrack.studentportal.dto.LoginResponseDTO;
import com.edutrack.studentportal.dto.UserDTO;
import com.edutrack.studentportal.exception.DuplicateResourceException;
import com.edutrack.studentportal.exception.InvalidCredentialsException;
import com.edutrack.studentportal.exception.ResourceNotFoundException;
import com.edutrack.studentportal.model.User;
import com.edutrack.studentportal.repository.StudentRepository;
import com.edutrack.studentportal.repository.UserRepository;
import com.edutrack.studentportal.security.JwtService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final JwtService jwtService;
    private final StudentRepository studentRepository;

    // Constructor Injection
    public UserService(UserRepository userRepository,
                       ModelMapper modelMapper,
                       JwtService jwtService,
                       StudentRepository studentRepository) {

        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.jwtService = jwtService;
        this.studentRepository = studentRepository;
    }

    // Create User
    public UserDTO saveUser(UserDTO userDTO){
        normalizeAndValidateRole(userDTO);

        if(userRepository.existsByUsername(userDTO.getUsername())){
            throw new DuplicateResourceException(
                    "username already exists"
            );
        }

        if(userRepository.existsByEmail(userDTO.getEmail())){
            throw new DuplicateResourceException(
                    "email already exists"
            );
        }

        if("STUDENT".equalsIgnoreCase((userDTO.getRole()))){

            if(userDTO.getStudentId() == null ||
                    !studentRepository.existsById(userDTO.getStudentId())){

                throw new ResourceNotFoundException(
                        "student not found with id : "+userDTO.getStudentId()
                );
            }
        }

        User user = modelMapper.map(userDTO, User.class);
        User savedUser = userRepository.save(user);

        return toUserDTO(savedUser);
    }

    // Get All Users
    public List<UserDTO> getAllUsers(){

        List<User> users = userRepository.findAll();

        List<UserDTO> userDTOS = new ArrayList<>();

        for(User user : users){
            userDTOS.add(toUserDTO(user));
        }

        return userDTOS;
    }

    // Get User By id
    public UserDTO getUserById(String id){

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id : " + id));

        return toUserDTO(user);
    }

    private void normalizeAndValidateRole(UserDTO userDTO) {
        String role = userDTO.getRole() == null ? "" : userDTO.getRole().toUpperCase();
        if (!List.of("ADMIN", "STUDENT", "INSTRUCTOR").contains(role)) {
            throw new com.edutrack.studentportal.exception.InvalidRequestException(
                    "Role must be ADMIN, STUDENT, or INSTRUCTOR");
        }
        userDTO.setRole(role);
        if ("STUDENT".equals(role)
                && (userDTO.getStudentId() == null || !studentRepository.existsById(userDTO.getStudentId()))) {
            throw new ResourceNotFoundException("A valid student profile is required for a STUDENT account");
        }
    }

    // Update User
    public UserDTO updateUser(String id, UserDTO userDTO){
        normalizeAndValidateRole(userDTO);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id : " + id));

        User user = modelMapper.map(userDTO, User.class);

        user.setId(existingUser.getId());
        if (userDTO.getPassword() == null || userDTO.getPassword().isBlank()) {
            user.setPassword(existingUser.getPassword());
        }

        User updatedUser = userRepository.save(user);

        return toUserDTO(updatedUser);
    }

    // Delete User
    public void deleteUser(String id){
        userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id : " + id));

        userRepository.deleteById(id);
    }

    // Search User By Username
    public List<UserDTO> searchUsers(String username){

        return userRepository.findByUsernameContainingIgnoreCase(username)
                .stream()
                .map(this::toUserDTO)
                .toList();
    }

    // Sort By Username
    public List<UserDTO> getUsersSortedByUsername(){

        List<User> users = userRepository.findAll(
                Sort.by("username").ascending()
        );

        return users.stream()
                .map(this::toUserDTO)
                .toList();
    }

    // Pagination
    public Page<UserDTO> getUsersWithPagination(int page, int size){

        Pageable pageable = PageRequest.of(page, size);

        Page<User> users = userRepository.findAll(pageable);

        return users.map(this::toUserDTO);
    }

    private UserDTO toUserDTO(User user) {
        UserDTO dto = modelMapper.map(user, UserDTO.class);
        dto.setPassword(null);
        return dto;
    }

    public LoginResponseDTO login(LoginDTO loginDTO){

        Optional<User> optionalUser =
                userRepository.findByUsername(loginDTO.getUsername());

        User user = optionalUser.orElseThrow(() ->
                new InvalidCredentialsException("Invalid password or username")
        );

        if (!Objects.equals(loginDTO.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password or username");
        }

        LoginResponseDTO response = modelMapper.map(user, LoginResponseDTO.class);

        String token = jwtService.generateToken(
                user.getUsername(),
                user.getRole()
        );

        response.setToken(token);

        return response;
    }

}