package info.jtrac.web.api;

import info.jtrac.domain.Space;
import info.jtrac.domain.User;
import info.jtrac.domain.UserSpaceRole;
import info.jtrac.service.JtracService;
import info.jtrac.web.api.dto.SpaceDto;
import info.jtrac.web.api.dto.UserSpaceRoleDto;
import info.jtrac.web.api.dto.UserSpaceRoleResponseDto;
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
public class SpaceController {

    private final JtracService jtracService;

    public SpaceController(JtracService jtracService) {
        this.jtracService = jtracService;
    }

    @GetMapping
    public ResponseEntity<List<Space>> findSpaces(@RequestParam(required = false) Long userId) {
        List<Space> spaces = jtracService.findSpaces(userId);
        return new ResponseEntity<>(spaces, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Space> getSpaceById(@PathVariable long id) {
        Space space = jtracService.loadSpace(id);
        if (space == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(space, HttpStatus.OK);
    }

    @GetMapping("/{id}/users")
    public ResponseEntity<List<UserSpaceRoleResponseDto>> getSpaceUsers(@PathVariable long id) {
        List<UserSpaceRole> userRoles = jtracService.findUserRolesForSpace(id);
        List<UserSpaceRoleResponseDto> dtos = userRoles.stream()
                .map(ur -> new UserSpaceRoleResponseDto(ur.getUser().getLoginName(), ur.getRoleKey()))
                .collect(Collectors.toList());
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

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

    @PutMapping("/{id}")
    public ResponseEntity<Space> updateSpace(@PathVariable long id, @RequestBody SpaceDto spaceDto) {
        Space space = jtracService.loadSpace(id);
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpace(@PathVariable long id) {
        jtracService.removeSpace(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{id}/users")
    public ResponseEntity<UserSpaceRole> addUserToSpace(@PathVariable long id, @RequestBody UserSpaceRoleDto userSpaceRoleDto) {
        Space space = jtracService.loadSpace(id);
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

    @PutMapping("/{spaceId}/users/{userId}")
    public ResponseEntity<UserSpaceRole> updateUserRoleInSpace(@PathVariable long spaceId, @PathVariable long userId, @RequestBody UserSpaceRoleDto userSpaceRoleDto) {
        UserSpaceRole userSpaceRole = jtracService.findUserSpaceRoleByUserIdAndSpaceId(userId, spaceId);
        if (userSpaceRole == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        userSpaceRole.setRoleKey(userSpaceRoleDto.getRoleKey());
        jtracService.storeUserSpaceRole(userSpaceRole);
        return new ResponseEntity<>(userSpaceRole, HttpStatus.OK);
    }
}
