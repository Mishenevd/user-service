package com.example;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfig {

    @Value("${spring.data.mongodb.host}")
    private String mongoHost;

    @Value("${spring.data.mongodb.port}")
    private int mongoPort;

    @Autowired
    private DirectExchange userExchange;

    @Bean
    public Queue testQueue() {
        return new Queue(userExchange.getName());
    }

    @Bean
    public MongoClient mongoClient() {
        return new MongoClient(new ServerAddress(mongoHost, mongoPort));
    }

}
