package com.example.userservice.rest;

import com.example.TestConfig;
import com.example.userservice.rabbit.message.UserCreated;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lordofthejars.nosqlunit.annotation.CustomComparisonStrategy;
import com.lordofthejars.nosqlunit.annotation.IgnorePropertyValue;
import com.lordofthejars.nosqlunit.annotation.ShouldMatchDataSet;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import com.lordofthejars.nosqlunit.mongodb.MongoFlexibleComparisonStrategy;
import com.mongodb.MongoClient;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import lombok.SneakyThrows;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.assertj.core.api.Assertions.assertThat;

@WebFluxTest(controllers = UserController.class)
@CustomComparisonStrategy(comparisonStrategy = MongoFlexibleComparisonStrategy.class)
@ComponentScan(value = {"com.example.userservice"})
@ContextConfiguration(initializers = UserControllerTest.Initializer.class, classes = TestConfig.class)
@TestPropertySource(locations = "classpath:application.yml")
@OverrideAutoConfiguration(enabled = true)
@RunWith(SpringRunner.class)
public class UserControllerTest {

    public static final String DATABASE_NAME = "user-create-test";

    @ClassRule
    public static RabbitMQContainer rabbit = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3-management"))
            .withExposedPorts(5672, 15672);

    @ClassRule
    public static MongoDBContainer mongo = new MongoDBContainer(DockerImageName.parse("mongo:3.2.0"))
            .withExposedPorts(27017);

    @Rule
    public MongoDbRule mongoRule = newMongoDbRule().defaultSpringMongoDb(DATABASE_NAME);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    public MongoClient mongoClient;

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Queue testQueue;

    @Autowired
    private ObjectMapper objectMapper;

    @ShouldMatchDataSet(location = "create_single_user_controller_test.json")
    @IgnorePropertyValue(properties = {"creationDate"})
    @Test
    public void testCreateSingleUser() {
        //language=json
        String json = "{\n" +
                      "    \"userName\": \"testUserName\",\n" +
                      "    \"name\": \"testFirstName\",\n" +
                      "    \"surname\": \"testLastName\"\n" +
                      "}";

        webClient.post()
                .uri("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(json), String.class)
                .exchange()
                .expectStatus().isOk();

        assertPublishedMessage(new UserCreated("testUserName"));
        assertNoMoreMessages();
    }

    @SneakyThrows
    private void assertPublishedMessage(UserCreated expected) {
        Message receive = rabbitTemplate.receive(testQueue.getName());
        UserCreated userCreated = objectMapper.readValue(Objects.requireNonNull(receive).getBody(), UserCreated.class);
        assertThat(userCreated.getUserName())
                .isEqualTo(expected.getUserName());
    }

    private void assertNoMoreMessages() {
        DeclareOk declareOk = rabbitTemplate.execute(channel -> channel.queueDeclarePassive(testQueue.getName()));
        assertThat(Objects.requireNonNull(declareOk).getMessageCount()).isEqualTo(0);
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues values = TestPropertyValues.of(
                    "spring.rabbitmq.port=" + rabbit.getMappedPort(5672),
                    "spring.data.mongodb.host=" + mongo.getHost(),
                    "spring.data.mongodb.port=" + mongo.getMappedPort(27017),
                    "spring.data.mongodb.database=" + DATABASE_NAME
            );
            values.applyTo(configurableApplicationContext);
        }
    }

}
