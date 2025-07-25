package info.jtrac.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.jtrac.config.SecurityConfig;
import info.jtrac.config.jwt.TokenProvider;
import info.jtrac.web.api.dto.AuthenticationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private TokenProvider tokenProvider;
    
    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    public void whenAuthenticateWithValidCredentials_thenReturnsJwtToken() throws Exception {
        // Arrange
        AuthenticationRequest authRequest = new AuthenticationRequest("admin", "admin");
        String dummyToken = "dummy-jwt-token";
        User user = new User("admin", "password", new ArrayList<>());

        given(userDetailsService.loadUserByUsername("admin")).willReturn(user);
        
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        given(authenticationManager.authenticate(any())).willReturn(authentication);
        given(tokenProvider.generateToken(any())).willReturn(dummyToken);

        // Act & Assert
        mockMvc.perform(post("/api/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(dummyToken));
    }


}
