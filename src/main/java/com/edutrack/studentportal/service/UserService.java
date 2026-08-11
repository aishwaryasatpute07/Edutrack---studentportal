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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final StudentRepository studentRepository;

    // Constructor Injection
    public UserService(UserRepository userRepository,
                       ModelMapper modelMapper,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder,
                       StudentRepository studentRepository) {

        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.studentRepository = studentRepository;
    }

    // Create User
    public UserDTO saveUser(UserDTO userDTO){

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

        user.setPassword(
                passwordEncoder.encode(userDTO.getPassword())
        );

        User savedUser = userRepository.save(user);

        return modelMapper.map(savedUser, UserDTO.class);
    }

    // Get All Users
    public List<UserDTO> getAllUsers(){

        List<User> users = userRepository.findAll();

        List<UserDTO> userDTOS = new ArrayList<>();

        for(User user : users){
            userDTOS.add(modelMapper.map(user, UserDTO.class));
        }

        return userDTOS;
    }

    // Get User By id
    public UserDTO getUserById(String id){

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id : " + id));

        return modelMapper.map(user, UserDTO.class);
    }

    // Update User
    public UserDTO updateUser(String id, UserDTO userDTO){

        User existingUser = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with id : " + id));

        User user = modelMapper.map(userDTO, User.class);

        user.setId(existingUser.getId());

        User updatedUser = userRepository.save(user);

        return modelMapper.map(updatedUser, UserDTO.class);
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
                .map(user -> modelMapper.map(user, UserDTO.class))
                .toList();
    }

    // Sort By Username
    public List<UserDTO> getUsersSortedByUsername(){

        List<User> users = userRepository.findAll(
                Sort.by("username").ascending()
        );

        return users.stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .toList();
    }

    // Pagination
    public Page<UserDTO> getUsersWithPagination(int page, int size){

        Pageable pageable = PageRequest.of(page, size);

        Page<User> users = userRepository.findAll(pageable);

        return users.map(user ->
                modelMapper.map(user, UserDTO.class));
    }

    public LoginResponseDTO login(LoginDTO loginDTO){

        System.out.println("Searching username: " + loginDTO.getUsername());

        Optional<User> optionalUser =
                userRepository.findByUsername(loginDTO.getUsername());

        System.out.println("User exists: " + optionalUser.isPresent());

        User user = optionalUser.orElseThrow(() ->
                new ResourceNotFoundException("User not found")
        );

        if (!passwordEncoder.matches(
                loginDTO.getPassword(),
                user.getPassword()
        )) {
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