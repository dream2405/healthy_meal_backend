package kr.ac.dankook.ace.healthy_meal_backend.controller;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.dankook.ace.healthy_meal_backend.dto.LoginRequestDTO;
import kr.ac.dankook.ace.healthy_meal_backend.dto.TokenResponseDTO;
import kr.ac.dankook.ace.healthy_meal_backend.dto.UserGetDTO;
import kr.ac.dankook.ace.healthy_meal_backend.dto.UserPostDTO;
import kr.ac.dankook.ace.healthy_meal_backend.entity.User;
import kr.ac.dankook.ace.healthy_meal_backend.repository.UserRepository;
import kr.ac.dankook.ace.healthy_meal_backend.security.JwtTokenProvider;

@Tag(name = "인증")
@RestController
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, UserRepository userRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/signup")
    @Operation(summary = "주어진 정보로 회원가입")
    public ResponseEntity<UserGetDTO> createUser(@RequestBody UserPostDTO userPost) {
        System.out.println("전달받은 DTO Id값: "+ userPost);
        if (userRepository.existsById(userPost.getId())) {
            System.out.println("중복아이디 오류!");
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        User user = new User();
        user.setId(userPost.getId());
        user.setHashedPassword(passwordEncoder.encode(userPost.getHashedPassword()));
        user.setBirthday(userPost.getBirthday());
        user.setGender(userPost.getGender() != null && !userPost.getGender().isEmpty() ? userPost.getGender().charAt(0) : null);

        var savedUser = userRepository.save(user);

        UserGetDTO userGetDTO = modelMapper.map(savedUser, UserGetDTO.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(userGetDTO);
    }

    @PostMapping("/login")
    @Operation(summary = "아이디/패스워드로 인증")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
        System.out.println("토큰발급 전 인증 진행 : "+request.getId()+", "+request.getPassword());
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getId(),
                request.getPassword()
            )
        );

        String token = jwtTokenProvider.createToken(auth.getName());

        return ResponseEntity.ok(new TokenResponseDTO(token));
    }

    @GetMapping("/api/test")
    public String hello() {
        return "Swagger Hello!";
    }
}
