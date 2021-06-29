package com.example.userservice.service;

import com.example.TestConfig;
import com.example.userservice.UserServiceApplication;
import com.lordofthejars.nosqlunit.annotation.CustomComparisonStrategy;
import com.lordofthejars.nosqlunit.annotation.ShouldMatchDataSet;
import com.lordofthejars.nosqlunit.mongodb.MongoFlexibleComparisonStrategy;
import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

import static io.specto.hoverfly.junit.core.SimulationSource.dsl;
import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static io.specto.hoverfly.junit.dsl.matchers.HoverflyMatchers.matches;

@SpringBootTest(classes = UserServiceApplication.class)
@CustomComparisonStrategy(comparisonStrategy = MongoFlexibleComparisonStrategy.class)
@ComponentScan(value = {"com.example.userservice"})
@ContextConfiguration(initializers = UserServiceTest.Initializer.class, classes = TestConfig.class)
@TestPropertySource(locations = "classpath:application.yml")
@OverrideAutoConfiguration(enabled = true)
@RunWith(SpringRunner.class)
public class UserServiceTest {

    public static final String DATABASE_NAME = "user-create-test";

    @ClassRule
    public static RabbitMQContainer rabbit = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3-management"))
            .withExposedPorts(5672, 15672);

    @ClassRule
    public static MongoDBContainer mongo = new MongoDBContainer(DockerImageName.parse("mongo:3.2.0"))
            .withExposedPorts(27017);

    @ClassRule
    public static final HoverflyRule hoverfly = HoverflyRule.inSimulationMode();

    @Autowired
    private UserService userService;

    @Autowired
    private DirectExchange userExchange;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @ShouldMatchDataSet(location = "create_single_user_controller_test.json")
    @Test
    public void testProcessUserCreated() {
        //language=json
        String json = "{" +
                      "    \"userName\": \"testUserName\"" +
                      "}";

        rabbitTemplate.convertAndSend(userExchange.getName(), json);

        hoverfly.simulate(dsl(service(matches("www.somedomain.test"))
                .post("/post")
                .body("{\"userName\":\"testUserName\"}")
                .willReturn(success().body(
                        //language=json
                        "{" +
                        "    \"message\": \"OK\"" +
                        "}"
                ).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))));

        StepVerifier.create(userService.processCreatedUsers())
                .expectNextCount(1)
                .thenCancel()
                .verify();
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
