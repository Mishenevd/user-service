package com.example.userservice.rabbit;

import com.example.userservice.rabbit.message.UserCreated;
import lombok.RequiredArgsConstructor;
import ma.glasnost.orika.MapperFacade;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@Component
public class EventListener {

    private final ConnectionFactory connectionFactory;

    private final Queue userQueue;

    private final MapperFacade mapperFacade;

    public Flux<UserCreated> getUserCreatedMessageStream() {
        return Flux.create(emitter -> {
            SimpleMessageListenerContainer mlc = new SimpleMessageListenerContainer(connectionFactory);
            mlc.addQueueNames(userQueue.getName());
            mlc.setupMessageListener(message -> emitter.next(mapperFacade.map(message, UserCreated.class)));
            emitter.onRequest(v -> mlc.start());
            emitter.onDispose(mlc::stop);
        });
    }

}
