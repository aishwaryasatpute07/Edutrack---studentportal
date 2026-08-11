package com.edutrack.studentportal.controller;

import com.edutrack.studentportal.dto.LoginDTO;
import com.edutrack.studentportal.dto.LoginResponseDTO;
import com.edutrack.studentportal.dto.UserDTO;
import com.edutrack.studentportal.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    // Constructor Injection
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Create User
    @PostMapping
    public UserDTO saveUser(@Valid @RequestBody UserDTO userDTO){
        return userService.saveUser(userDTO);
    }

    // Get All Users
    @GetMapping
    public List<UserDTO> getAllUsers(){
        return userService.getAllUsers();
    }

    // Get User By Id
    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable String id){
        return userService.getUserById(id);
    }

    // Search User By Username
    @GetMapping("/search")
    public List<UserDTO> searchUsers(@RequestParam String username){
        return userService.searchUsers(username);
    }

    // Sort Users By Username
    @GetMapping("/sort/username")
    public List<UserDTO> getUsersSortedByUsername(){
        return userService.getUsersSortedByUsername();
    }

    // Pagination
    @GetMapping("/pagination")
    public Page<UserDTO> getUsersWithPagination(
            @RequestParam int page,
            @RequestParam int size){

        return userService.getUsersWithPagination(page, size);
    }

    // Update User
    @PutMapping("/{id}")
    public UserDTO updateUser(
            @PathVariable String id,
            @Valid @RequestBody UserDTO userDTO){

        return userService.updateUser(id, userDTO);
    }

    // Delete User
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable String id){

        userService.deleteUser(id);

        return "User deleted successfully";
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginDTO loginDTO){

        return userService.login(loginDTO);
    }
}