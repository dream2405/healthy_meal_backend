package kr.ac.dankook.ace.healthy_meal_backend;

import kr.ac.dankook.ace.healthy_meal_backend.controller.AuthController;
import kr.ac.dankook.ace.healthy_meal_backend.repository.UserRepository;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @MockitoBean
    private UserRepository userRepository;


}
