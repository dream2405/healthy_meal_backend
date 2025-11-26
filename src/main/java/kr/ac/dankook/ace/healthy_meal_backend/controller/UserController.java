package kr.ac.dankook.ace.healthy_meal_backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.dankook.ace.healthy_meal_backend.dto.ResultMessageResponseDTO;
import kr.ac.dankook.ace.healthy_meal_backend.dto.UserResponseDTO;
import kr.ac.dankook.ace.healthy_meal_backend.dto.UserUpdateRequestDTO;
import kr.ac.dankook.ace.healthy_meal_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "유저")
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUser(@PathVariable String userId) {
        UserResponseDTO userResponseDTO = userService.getUser(userId);
        return ResponseEntity.ok(userResponseDTO);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<ResultMessageResponseDTO> updateUser(
            @PathVariable String userId,
            @RequestBody UserUpdateRequestDTO userUpdateRequestDTO) {
        userService.updateUser(userId, userUpdateRequestDTO);
        return ResponseEntity.ok(new ResultMessageResponseDTO("User updated successfully"));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ResultMessageResponseDTO> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(new ResultMessageResponseDTO("User deleted successfully"));
    }
}