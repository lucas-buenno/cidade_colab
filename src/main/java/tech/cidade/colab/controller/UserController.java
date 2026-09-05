package tech.cidade.colab.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.cidade.colab.service.UserService;
import tech.cidade.colab.dto.request.CreateUserRequest;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    ResponseEntity<Void> createUser(@RequestBody @Validated CreateUserRequest request) {
        String id = userService.createUser(request);
        return ResponseEntity.created(URI.create("/users/" + id)).build();
    }
}
