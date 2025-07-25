package info.jtrac.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.jtrac.domain.User;
import info.jtrac.service.JtracService;
import info.jtrac.web.api.dto.AuthenticationRequest;
import info.jtrac.web.api.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JtracService jtracService;

    private String jwtToken;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setup() throws Exception {
        org.springframework.security.core.userdetails.User user = new org.springframework.security.core.userdetails.User("admin", passwordEncoder.encode("admin"), new ArrayList<>());
        given(userDetailsService.loadUserByUsername("admin")).willReturn(user);


        AuthenticationRequest authRequest = new AuthenticationRequest("admin", "admin");
        MvcResult result = mockMvc.perform(post("/api/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andReturn();
        String response = result.getResponse().getContentAsString();
        jwtToken = objectMapper.readTree(response).get("token").asText();
    }

    @Test
    public void whenCreateAndGetUser_thenCorrect() throws Exception {
        // Arrange
        UserDto userDto = new UserDto();
        userDto.setLoginName("testuser");
        userDto.setName("Test User");
        userDto.setEmail("test@jtrac.info");
        userDto.setPassword("password");

        // Act & Assert Create
        mockMvc.perform(post("/api/users")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated());

        // Act & Assert Get
        mockMvc.perform(get("/api/users/testuser")
                .header("Authorization", "Bearer " + jwtToken)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginName").value("testuser"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@jtrac.info"));

        // Cleanup
        User user = jtracService.findUserByLoginName("testuser");
        if (user != null) {
            // jtracService.removeUser(user.getId()); // Assuming there is a removeUser method
        }
    }
}
