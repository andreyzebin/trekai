package info.jtrac.web.api;

import info.jtrac.domain.User;
import info.jtrac.service.JtracService;
import info.jtrac.web.api.dto.UserDto;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final JtracService jtracService;
    private final PasswordEncoder passwordEncoder;

    public UserController(JtracService jtracService, PasswordEncoder passwordEncoder) {
        this.jtracService = jtracService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    public ResponseEntity<User> addUser(@RequestBody UserDto userDto) {
        User user = new User();
        user.setLoginName(userDto.getLoginName());
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setAvatarUrl(userDto.getAvatarUrl());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        jtracService.saveUser(user);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PutMapping("/{loginName}")
    public ResponseEntity<User> updateUser(@PathVariable String loginName, @RequestBody UserDto userDto) {
        User user = jtracService.findUserByLoginName(loginName);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if(userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if(userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        if(userDto.getAvatarUrl() != null) {
            user.setAvatarUrl(userDto.getAvatarUrl());
        }

        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
        jtracService.saveUser(user);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("/{loginName}")
    public ResponseEntity<User> getUser(@PathVariable String loginName) {
        User user = jtracService.findUserByLoginName(loginName);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
}
