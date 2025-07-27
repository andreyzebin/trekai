package info.jtrac.web.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.jtrac.service.JtracService;
import info.jtrac.web.api.dto.AuthenticationRequest;
import info.jtrac.web.api.dto.CustomFieldDto;
import info.jtrac.web.api.dto.ItemCreateDto;
import info.jtrac.web.api.dto.ItemPatchDto;
import info.jtrac.web.api.dto.SpaceDto;
import info.jtrac.web.api.dto.UserDto;
import info.jtrac.web.api.dto.UserSpaceRoleDto;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class WebSpaceControllerTest {

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
    public void whenCreateSpaceAndAddUser_thenCorrect() throws Exception {
        // 1. Create a user
        UserDto userDto = new UserDto();
        userDto.setLoginName("testuser");
        userDto.setName("Test User");
        userDto.setEmail("test@jtrac.info");
        userDto.setPassword("password");

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andReturn();


        // 2. Create a space
        SpaceDto spaceDto = new SpaceDto();
        spaceDto.setPrefixCode("TEST");
        spaceDto.setName("Test Space");

        MvcResult spaceResult = mockMvc.perform(post("/api/spaces")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(spaceDto)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andReturn();

        String spaceResponse = spaceResult.getResponse().getContentAsString();
        JsonNode spaceNode = objectMapper.readTree(spaceResponse);
        String spaceId = spaceNode.get("prefixCode").textValue();

        // 3. Add user to space with a role
        UserSpaceRoleDto userSpaceRoleDto = new UserSpaceRoleDto();
        userSpaceRoleDto.setLoginName("testuser");
        userSpaceRoleDto.setRoleKey("ROLE_USER");

        mockMvc.perform(post("/api/spaces/" + spaceId + "/users")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userSpaceRoleDto)))
                .andExpect(status().isCreated())
                .andDo(print());

        // 4. Verify using API
        mockMvc.perform(get("/api/spaces/" + spaceId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prefixCode", is("TEST")))
                .andExpect(jsonPath("$.name", is("Test Space")))
                .andDo(print());

        mockMvc.perform(get("/api/spaces/" + spaceId + "/users")
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].roleKey", is("ROLE_USER")))
                .andExpect(jsonPath("$[0].loginName", is("testuser")))
                .andDo(print());
    }

    @Test
    public void testAddCustomFieldToSpace() throws Exception {
        // 1. Create a new space
        SpaceDto spaceDto = new SpaceDto();
        spaceDto.setPrefixCode("CUSTOM");
        spaceDto.setName("Custom Field Space");
        mockMvc.perform(post("/api/spaces")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(spaceDto)))
                .andExpect(status().isCreated())
                .andDo(print());

        // 2. Add a custom field to it
        CustomFieldDto customFieldDto = new CustomFieldDto();
        customFieldDto.setName("customerName");
        customFieldDto.setLabel("Customer Name");
        customFieldDto.setType(2); // Text

        mockMvc.perform(post("/api/spaces/CUSTOM/fields")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customFieldDto)))
                .andExpect(status().isCreated())
                .andDo(print());

        // 3. Verify the field was added
        mockMvc.perform(get("/api/spaces/CUSTOM")
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.fields['customerName'].label", is("Customer Name")))
                .andDo(print());

        // 4. Create Item
        ItemCreateDto createDto = new ItemCreateDto();
        createDto.setSpacePrefix("CUSTOM");
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

        ItemPatchDto patchDto = new ItemPatchDto();
        patchDto.setCustomFields(Map.of("customerName", "John Doe"));

        mockMvc.perform(patch("/api/items/" + itemId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchDto)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        // 2. Get Item
        mockMvc.perform(get("/api/items/" + itemId)
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fieldValues['customerName']", is("John Doe")))
                .andDo(print());
    }
}
