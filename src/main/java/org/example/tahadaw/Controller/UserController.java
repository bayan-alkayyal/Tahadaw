package org.example.tahadaw.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tahadaw.Api.ApiResponse;
import org.example.tahadaw.DTO.IN.UserDTOIn;
import org.example.tahadaw.Service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {


    private final UserService userService;

    @PostMapping("/add")
    public ResponseEntity<?> addUser(@Valid @RequestBody UserDTOIn userDTOIn) {
        userService.addUser(userDTOIn);
        return ResponseEntity.status(200).body(new ApiResponse("User added successfully"));
    }

    @GetMapping("/get")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.status(200).body(userService.getAllUsers());
    }

    @PutMapping("/update/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @Valid @RequestBody UserDTOIn userDTOIn) {
        userService.updateUser(userId, userDTOIn);
        return ResponseEntity.status(200).body(new ApiResponse("User updated successfully"));
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.status(200).body(new ApiResponse("User deleted successfully"));
    }


}
