package com.example.userservice.rest;

import com.example.userservice.dto.UserDto;
import com.example.userservice.model.ExternalResponse;
import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping
    public Flux<UserDto> getAllLike(@RequestParam(defaultValue = "") String userName) {
        return userService.findAllLike(userName);
    }

    @GetMapping("/{id}")
    public Mono<UserDto> get(@PathVariable String id) {
        return userService.getById(id);
    }

    @PutMapping("/{id}")
    public Mono<Void> update(@PathVariable String id, @RequestBody UserDto user) {
        return userService.updateById(id, user);
    }

    @DeleteMapping
    public Mono<Void> delete() {
        return userService.delete();
    }

    @GetMapping(value = "/process", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    public Flux<ExternalResponse> process() {
        return userService.processCreatedUsers();
    }

}
