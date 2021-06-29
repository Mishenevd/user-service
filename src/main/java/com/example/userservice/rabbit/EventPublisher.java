package com.example.userservice.rabbit;

import com.example.userservice.rabbit.message.UserCreated;
import lombok.RequiredArgsConstructor;
import ma.glasnost.orika.MapperFacade;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    private final DirectExchange userExchange;

    private final MapperFacade mapperFacade;

    public Mono<Void> publishUserCreated(UserCreated message) {
        return Mono.fromRunnable(() -> {
            String json = mapperFacade.map(message, String.class);
            rabbitTemplate.convertAndSend(userExchange.getName(), json);
        });
    }

}
