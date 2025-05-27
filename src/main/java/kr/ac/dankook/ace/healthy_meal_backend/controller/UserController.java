package kr.ac.dankook.ace.healthy_meal_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.dankook.ace.healthy_meal_backend.dto.MealInfoPostDTO;
import kr.ac.dankook.ace.healthy_meal_backend.dto.UserGetDTO;
import kr.ac.dankook.ace.healthy_meal_backend.entity.MealInfo;
import kr.ac.dankook.ace.healthy_meal_backend.entity.User;
import kr.ac.dankook.ace.healthy_meal_backend.dto.UserPostDTO;
import kr.ac.dankook.ace.healthy_meal_backend.repository.MealInfoRepository;
import kr.ac.dankook.ace.healthy_meal_backend.repository.UserRepository;
import kr.ac.dankook.ace.healthy_meal_backend.service.StorageService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/users")
@Tag(name = "유저")
public class UserController {
    private final UserRepository userRepository;
    private final MealInfoRepository mealInfoRepository;
    private final StorageService storageService;
    private final ModelMapper modelMapper;

    @Autowired
    public UserController(
            UserRepository userRepository,
            MealInfoRepository mealInfoRepository,
            StorageService storageService,
            ModelMapper modelMapper
    ) {
        this.userRepository = userRepository;
        this.mealInfoRepository = mealInfoRepository;
        this.storageService = storageService;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    @Operation(summary = "주어진 정보로 회원가입")
    public ResponseEntity<UserGetDTO> createUser(@RequestBody UserPostDTO userPost) {
        if (userRepository.existsById(userPost.getId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        User user = new User();
        user.setId(userPost.getId());
        user.setHashedPassword(userPost.getHashedPassword());
        user.setBirthday(userPost.getBirthday());
        user.setGender(userPost.getGender());

        var savedUser = userRepository.save(user);

        UserGetDTO userGetDTO = modelMapper.map(savedUser, UserGetDTO.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(userGetDTO);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "주어진 ID를 가진 특정 유저 가져오기")
    public ResponseEntity<UserGetDTO> getUser(@PathVariable String userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        UserGetDTO userGetDTO = modelMapper.map(user.get(), UserGetDTO.class);
        return ResponseEntity.status(HttpStatus.OK).body(userGetDTO);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "주어진 ID를 가진 유저 삭제")
    public ResponseEntity<Object> deleteUser(@PathVariable String userId) {
        if (!userRepository.existsById(userId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        userRepository.deleteById(userId);
        return ResponseEntity.status(HttpStatus.valueOf(204)).build();
    }

    @GetMapping("/{userId}/meal-info")
    @Operation(summary = "주어진 ID의 유저가 기록한 모든 식단 정보 가져오기")
    public ResponseEntity<List<MealInfoPostDTO>> getMealInfo(
            @PathVariable String userId,
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
    ) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        List<MealInfo> mealInfos = mealInfoRepository.findByUserIdAndCreatedDate(userId, date);
        List<MealInfoPostDTO> mealInfoPostDTOs = mealInfos.stream()
                .map(mealInfo -> modelMapper.map(mealInfo, MealInfoPostDTO.class)).toList();
        return ResponseEntity.status(HttpStatus.OK).body(mealInfoPostDTOs);
    }

    @PostMapping(value = "/{userId}/meal-info", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "주어진 정보로 주어진 ID의 유저가 식단정보 기록")
    public ResponseEntity<MealInfoPostDTO> createMealInfo(
            @PathVariable String userId,
            @RequestPart("img") MultipartFile file,
            @RequestPart("intake_amount") String amount,
            @RequestPart("diary") String diary
    ) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        String fileName = storageService.store(file);
        Integer intakeAmount = Integer.parseInt(amount);

        MealInfo mealInfo = new MealInfo();
        mealInfo.setImgPath(fileName);
        mealInfo.setIntakeAmount(intakeAmount);
        mealInfo.setDiary(diary);
        mealInfo.setUser(user.get());

        mealInfoRepository.save(mealInfo);
        mealInfo = mealInfoRepository.findById(mealInfo.getId()).get();
        user.get().addMealInfo(mealInfo);

        MealInfoPostDTO mealInfoPostDTO = modelMapper.map(mealInfo, MealInfoPostDTO.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(mealInfoPostDTO);
    }

    @GetMapping("/{userId}/meal-info/{mealInfoId}")
    @Operation(summary = "주어진 ID의 유저가 기록한 주어진 ID의 식단 정보 가져오기")
    public ResponseEntity<MealInfoPostDTO> getMealInfo(@PathVariable String userId, @PathVariable Long mealInfoId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Optional<MealInfo> mealInfo = user.get().getMealInfos().stream()
                .filter(mf -> Objects.equals(mf.getId(), mealInfoId)).findFirst();
        return mealInfo
                .map(info -> ResponseEntity.status(HttpStatus.OK).body(modelMapper.map(info, MealInfoPostDTO.class)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
