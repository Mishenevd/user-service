package com.example.userservice.rest;

import com.example.userservice.dto.UserDto;
import com.example.userservice.model.ExternalResponse;
import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RequestMapping("/api/user")
@RestController
public class UserController {

    private final UserService userService;

    @PostMapping
    public Mono<Void> post(@RequestBody UserDto userDto) {
        return userService.createUser(userDto);
    }

    @GetMapping(value = "/process", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<ExternalResponse> process() {
        return userService.processCreatedUsers();
    }

}
