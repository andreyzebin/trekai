package info.jtrac.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.jtrac.domain.Space;
import info.jtrac.domain.User;
import info.jtrac.service.JtracService;
import info.jtrac.web.api.dto.AuthenticationRequest;
import info.jtrac.web.api.dto.ItemCreateDto;
import info.jtrac.web.api.dto.ItemUpdateDto;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ApiItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JtracService jtracService;

    private String jwtToken;
    private User testUser;
    private Space testSpace;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setup() throws Exception {
        // Admin user for setup
        org.springframework.security.core.userdetails.User adminUser = new org.springframework.security.core.userdetails.User("admin", passwordEncoder.encode("admin"), new ArrayList<>());
        given(userDetailsService.loadUserByUsername("admin")).willReturn(adminUser);

        AuthenticationRequest authRequest = new AuthenticationRequest("admin", "admin");
        MvcResult result = mockMvc.perform(post("/api/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andReturn();
        String response = result.getResponse().getContentAsString();
        jwtToken = objectMapper.readTree(response).get("token").asText();

        // Test user
        testUser = new User();
        testUser.setLoginName("testuser");
        testUser.setName("Test User");
        testUser.setEmail("test@jtrac.info");
        jtracService.saveUser(testUser);

        // Test space
        testSpace = new Space();
        testSpace.setPrefixCode("TEST");
        testSpace.setName("Test Space");
        jtracService.storeSpace(testSpace);
    }

    @Test
    public void testItemApiWorkflow() throws Exception {
        // 1. Create Item
        ItemCreateDto createDto = new ItemCreateDto();
        createDto.setSpaceId(testSpace.getId());
        createDto.setSummary("Test Summary");
        createDto.setDetail("Test Detail");
        createDto.setAssignedToId(testUser.getId());

        MvcResult createResult = mockMvc.perform(post("/api/items")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.summary", is("Test Summary")))
                .andReturn();

        long itemId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // 2. Get Item
        mockMvc.perform(get("/api/items/" + itemId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) itemId)))
                .andExpect(jsonPath("$.summary", is("Test Summary")));

        // 3. Update Item (edit, change assignee, change status, add comment)
        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setSummary("Updated Summary");
        updateDto.setStatus(1); // Example status
        updateDto.setComment("This is an update comment.");

        mockMvc.perform(put("/api/items/" + itemId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary", is("Updated Summary")))
                .andExpect(jsonPath("$.status", is(1)));

        // 4. Get list of items with filter
        mockMvc.perform(get("/api/items")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("spaceId", String.valueOf(testSpace.getId()))
                        .param("summary", "Updated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].summary", is("Updated Summary")));
    }
}
