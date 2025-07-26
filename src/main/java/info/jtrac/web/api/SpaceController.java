package info.jtrac.web.api;

import info.jtrac.domain.Space;
import info.jtrac.domain.User;
import info.jtrac.domain.UserSpaceRole;
import info.jtrac.service.JtracService;
import info.jtrac.web.api.dto.SpaceDto;
import info.jtrac.web.api.dto.UserSpaceRoleDto;
import info.jtrac.web.api.dto.UserSpaceRoleResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/spaces")
@Tag(name = "Spaces API", description = "Endpoints for managing spaces")
@SecurityRequirement(name = "bearerAuth")
public class SpaceController {

    private final JtracService jtracService;

    public SpaceController(JtracService jtracService) {
        this.jtracService = jtracService;
    }

    @Operation(summary = "Find spaces with optional filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval")
    })
    @GetMapping
    public ResponseEntity<List<Space>> findSpaces(
            @Parameter(description = "ID of the user to filter spaces by") @RequestParam(required = false) Long userId) {
        List<Space> spaces = jtracService.findSpaces(userId);
        return new ResponseEntity<>(spaces, HttpStatus.OK);
    }

    @Operation(summary = "Get a space by its prefix code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval"),
            @ApiResponse(responseCode = "404", description = "Space not found")
    })
    @GetMapping("/{prefixCode}")
    public ResponseEntity<Space> getSpaceByPrefixCode(@PathVariable String prefixCode) {
        Space space = jtracService.findSpaceByPrefixCode(prefixCode);
        if (space == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(space, HttpStatus.OK);
    }

    @Operation(summary = "Get users and their roles within a space")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval"),
            @ApiResponse(responseCode = "404", description = "Space not found")
    })
    @GetMapping("/{prefixCode}/users")
    public ResponseEntity<List<UserSpaceRoleResponseDto>> getSpaceUsers(@PathVariable String prefixCode) {
        Space space = jtracService.findSpaceByPrefixCode(prefixCode);
        if (space == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<UserSpaceRole> userRoles = jtracService.findUserRolesForSpace(space.getId());
        List<UserSpaceRoleResponseDto> dtos = userRoles.stream()
                .map(ur -> new UserSpaceRoleResponseDto(ur.getUser().getLoginName(), ur.getRoleKey()))
                .collect(Collectors.toList());
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @Operation(summary = "Create a new space")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Space created successfully")
    })
    @PostMapping
    public ResponseEntity<Space> createSpace(@RequestBody SpaceDto spaceDto) {
        Space space = new Space();
        space.setPrefixCode(spaceDto.getPrefixCode());
        space.setName(spaceDto.getName());
        space.setDescription(spaceDto.getDescription());
        space.setGuestAllowed(spaceDto.isGuestAllowed());
        jtracService.storeSpace(space);
        return new ResponseEntity<>(space, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing space")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Space updated successfully"),
            @ApiResponse(responseCode = "404", description = "Space not found")
    })
    @PutMapping("/{prefixCode}")
    public ResponseEntity<Space> updateSpace(@PathVariable String prefixCode, @RequestBody SpaceDto spaceDto) {
        Space space = jtracService.findSpaceByPrefixCode(prefixCode);
        if (space == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        space.setPrefixCode(spaceDto.getPrefixCode());
        space.setName(spaceDto.getName());
        space.setDescription(spaceDto.getDescription());
        space.setGuestAllowed(spaceDto.isGuestAllowed());
        jtracService.storeSpace(space);
        return new ResponseEntity<>(space, HttpStatus.OK);
    }

    @Operation(summary = "Delete a space")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Space deleted successfully")
    })
    @DeleteMapping("/{prefixCode}")
    public ResponseEntity<Void> deleteSpace(@PathVariable String prefixCode) {
        jtracService.removeSpaceByPrefixCode(prefixCode);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(summary = "Add a user to a space with a specific role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User added to space successfully"),
            @ApiResponse(responseCode = "404", description = "Space or User not found")
    })
    @PostMapping("/{prefixCode}/users")
    public ResponseEntity<UserSpaceRole> addUserToSpace(@PathVariable String prefixCode, @RequestBody UserSpaceRoleDto userSpaceRoleDto) {
        Space space = jtracService.findSpaceByPrefixCode(prefixCode);
        if (space == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        User user = jtracService.findUserByLoginName(userSpaceRoleDto.getLoginName());
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        UserSpaceRole userSpaceRole = new UserSpaceRole(user, space, userSpaceRoleDto.getRoleKey());
        jtracService.storeUserSpaceRole(userSpaceRole);
        return new ResponseEntity<>(userSpaceRole, HttpStatus.CREATED);
    }

    @Operation(summary = "Update a user's role in a space")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User role updated successfully"),
            @ApiResponse(responseCode = "404", description = "User/space role mapping not found")
    })
    @PutMapping("/{prefixCode}/users/{userId}")
    public ResponseEntity<UserSpaceRole> updateUserRoleInSpace(@PathVariable String prefixCode, @PathVariable long userId, @RequestBody UserSpaceRoleDto userSpaceRoleDto) {
        Space space = jtracService.findSpaceByPrefixCode(prefixCode);
        if (space == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        UserSpaceRole userSpaceRole = jtracService.findUserSpaceRoleByUserIdAndSpaceId(userId, space.getId());
        if (userSpaceRole == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        userSpaceRole.setRoleKey(userSpaceRoleDto.getRoleKey());
        jtracService.storeUserSpaceRole(userSpaceRole);
        return new ResponseEntity<>(userSpaceRole, HttpStatus.OK);
    }
}