package info.jtrac.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.jtrac.domain.Space;
import info.jtrac.domain.User;
import info.jtrac.repository.SpaceRepository;
import info.jtrac.repository.UserRepository;
import info.jtrac.service.JtracService;
import info.jtrac.web.api.dto.AuthenticationRequest;
import info.jtrac.web.api.dto.ItemCreateDto;
import info.jtrac.web.api.dto.ItemPatchDto;
import info.jtrac.web.api.dto.ItemUpdateDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
    private UserRepository userRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private JtracService jtracService;

    private String jwtToken;
    private User testUser;
    private User admin;
    private Space testSpace;

    @MockitoBean
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
        userRepository.save(testUser);

        // Admin user
        admin = new User();
        admin.setLoginName("admin");
        admin.setName("Admin");
        admin.setEmail("admin@jtrac.info");
        userRepository.save(admin);

        // Test space
        testSpace = new Space();
        testSpace.setPrefixCode("TEST");
        testSpace.setName("Test Space");
        jtracService.storeSpace(testSpace); // storeSpace handles sequence creation
    }

    @Test
    public void testItemApiWorkflow() throws Exception {
        // 1. Create Item
        ItemCreateDto createDto = new ItemCreateDto();
        createDto.setSpacePrefix(testSpace.getPrefixCode());
        createDto.setSummary("Test Summary");
        createDto.setDetail("Test Detail");
        createDto.setAssignedToId(testUser.getId());

        MvcResult createResult = mockMvc.perform(post("/api/items")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.summary", is("Test Summary")))
                .andDo(print())
                .andReturn();

        long itemId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        // 2. Get Item
        mockMvc.perform(get("/api/items/" + itemId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) itemId)))
                .andExpect(jsonPath("$.summary", is("Test Summary")))
                .andDo(print());

        // 3. Update Item (edit, change assignee, change status, add comment)
        ItemUpdateDto updateDto = new ItemUpdateDto();
        updateDto.setSummary("Updated Summary");
        updateDto.setStatus(1); // Example status
        updateDto.setComment("This is an update comment.");

        mockMvc.perform(put("/api/items/" + itemId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary", is("Updated Summary")))
                .andExpect(jsonPath("$.status", is(1)))
                .andDo(print());

        // 4. Get list of items with filter
        mockMvc.perform(get("/api/items")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("spaceId", String.valueOf(testSpace.getId()))
                        .param("summary", "Updated")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].summary", is("Updated Summary")))
                .andDo(print());
    }

    @Test
    public void testItemApiWorkflowAssign() throws Exception {
        // 1. Create Item
        ItemCreateDto createDto = new ItemCreateDto();
        createDto.setSpacePrefix(testSpace.getPrefixCode());
        createDto.setSummary("Test Summary");
        createDto.setDetail("Test Detail");

        MvcResult createResult = mockMvc.perform(post("/api/items")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.summary", is("Test Summary")))
                .andDo(print())
                .andReturn();

        long itemId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        ItemPatchDto patch = new ItemPatchDto();
        patch.setAssignedToId(admin.getId());


        // 2. Patch Item
        mockMvc.perform(patch("/api/items/" + itemId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andDo(print());

        // 4. Get list of items with filter
        mockMvc.perform(get("/api/items/" + itemId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedTo", is("admin")))
                .andDo(print());

    }

    @Test
    public void testItemApiWorkflowWrongField() throws Exception {
        // 1. Create Item
        ItemCreateDto createDto = new ItemCreateDto();
        createDto.setSpacePrefix(testSpace.getPrefixCode());
        createDto.setSummary("Test Summary");
        createDto.setDetail("Test Detail");

        MvcResult createResult = mockMvc.perform(post("/api/items")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.summary", is("Test Summary")))
                .andDo(print())
                .andReturn();

        long itemId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        ItemPatchDto patch = new ItemPatchDto();
        patch.setCustomFields(Map.of("nonExistingField", "no"));


        // 2. Patch Item
        mockMvc.perform(patch("/api/items/" + itemId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patch)))
                .andExpect(status().is4xxClientError())
                .andDo(print());

        // 4. Get list of items with filter
        mockMvc.perform(get("/api/items/" + itemId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.fieldValues['nonExistingField']").doesNotExist())
                .andDo(print());

    }
}
