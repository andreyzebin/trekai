package info.jtrac.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.jtrac.config.jwt.TokenProvider;
import info.jtrac.web.api.dto.AuthenticationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthenticationControllerSpringTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TokenProvider tokenProvider;
    
    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void whenAuthenticateWithValidCredentials_thenReturnsJwtToken() throws Exception {
        // Arrange
        AuthenticationRequest authRequest = new AuthenticationRequest("admin", "admin");
        String dummyToken = "dummy-jwt-token";
        User user = new User("admin", passwordEncoder.encode("admin"), new ArrayList<>());

        given(userDetailsService.loadUserByUsername("admin")).willReturn(user);
        given(tokenProvider.generateToken(any())).willReturn(dummyToken);

        // Act & Assert
        mockMvc.perform(post("/api/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(dummyToken));
    }

    @Test
    public void whenAuthenticateWithInvalidCredentials_thenReturnsUnauthorized() throws Exception {
        // Arrange
        AuthenticationRequest authRequest = new AuthenticationRequest("wronguser", "wrongpassword");

        given(userDetailsService.loadUserByUsername(anyString()))
                .willThrow(new UsernameNotFoundException("User not found"));

        // Act & Assert
        mockMvc.perform(post("/api/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized());
    }
}
