package com.example.userservice.configuration;

import com.example.userservice.dto.UserDto;
import com.example.userservice.model.User;
import com.example.userservice.rabbit.message.UserCreated;
import com.example.userservice.repository.RepositoryPackageMarker;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import ma.glasnost.orika.CustomConverter;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.Type;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableMongoRepositories(basePackageClasses = RepositoryPackageMarker.class)
public class ApplicationConfiguration {

    @Value("${user.exchange}")
    private String userExchange;

    @Bean
    public MapperFacade mapperFacade() {
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();

        mapperFactory.getConverterFactory().registerConverter(new CustomConverter<UserDto, User>() {
            @Override
            public User convert(UserDto source,
                                Type<? extends User> destinationType,
                                MappingContext mappingContext) {
                User user = new User();
                user.setUserName(source.getUserName());
                user.setFirstName(source.getName());
                user.setLastName(source.getSurname());
                return user;
            }
        });

        mapperFactory.getConverterFactory().registerConverter(new CustomConverter<User, UserCreated>() {
            @Override
            public UserCreated convert(User source,
                                       Type<? extends UserCreated> destinationType,
                                       MappingContext mappingContext) {
                UserCreated userCreated = new UserCreated();
                userCreated.setUserName(source.getUserName());
                return userCreated;
            }
        });

        mapperFactory.getConverterFactory().registerConverter(new CustomConverter<UserCreated, String>() {
            @SneakyThrows
            @Override
            public String convert(UserCreated source,
                                  Type<? extends String> destinationType,
                                  MappingContext mappingContext) {
                return objectMapper().writeValueAsString(source);
            }
        });

        mapperFactory.getConverterFactory().registerConverter(new CustomConverter<Message, UserCreated>() {
            @SneakyThrows
            @Override
            public UserCreated convert(Message source,
                                       Type<? extends UserCreated> destinationType,
                                       MappingContext mappingContext) {
                return objectMapper().readValue(source.getBody(), UserCreated.class);
            }
        });

        return mapperFactory.getMapperFacade();
    }

    @Bean
    public DirectExchange userExchange() {
        return new DirectExchange(userExchange);
    }

    @Bean
    public Queue userQueue() {
        return new Queue(userExchange);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
