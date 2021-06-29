package com.example.userservice.service;

import com.example.userservice.dto.UserDto;
import com.example.userservice.integration.ExternalServiceHttpClient;
import com.example.userservice.model.ExternalResponse;
import com.example.userservice.model.User;
import com.example.userservice.rabbit.EventListener;
import com.example.userservice.rabbit.EventPublisher;
import com.example.userservice.rabbit.message.UserCreated;
import com.example.userservice.repository.ExternalResponseRepository;
import com.example.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import ma.glasnost.orika.MapperFacade;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class UserService {

    private final MapperFacade mapperFacade;

    private final UserRepository userRepository;

    private final ExternalResponseRepository externalResponseRepository;

    private final EventPublisher eventPublisher;

    private final EventListener eventListener;

    private final ExternalServiceHttpClient externalServiceHttpClient;

    public Mono<Void> createUser(UserDto userDto) {
        User user = mapperFacade.map(userDto, User.class);
        user.setCreationDate(LocalDateTime.now());
        return Mono.just(userRepository.save(user))
                .map(u -> mapperFacade.map(u, UserCreated.class))
                .flatMap(eventPublisher::publishUserCreated);
    }

    public Flux<ExternalResponse> processCreatedUsers() {
        return eventListener.getUserCreatedMessageStream()
                .flatMap(userCreated -> Mono.just(externalServiceHttpClient.post(userCreated)))
                .map(response -> mapperFacade.map(response, ExternalResponse.class))
                .flatMap(response -> Mono.just(externalResponseRepository.save(response)));
    }

}
