package kr.ac.dankook.ace.healthy_meal_backend.controller;

import kr.ac.dankook.ace.healthy_meal_backend.exception.DuplicateUserIdException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
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
        logger.info("회원가입 시작 - 전달받은 DTO: {}",  userPost);
        if (userRepository.existsById(userPost.getId())) {
            throw new DuplicateUserIdException(userPost.getId());
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
        logger.info("토큰 발급 전 인증 진행: {} {}", request.getId(), request.getPassword());

        Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getId(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("로그인 실패: " + e.getMessage());
        }

        String token = jwtTokenProvider.createToken(auth.getName());

        return ResponseEntity.ok(new TokenResponseDTO(token));
    }
}
