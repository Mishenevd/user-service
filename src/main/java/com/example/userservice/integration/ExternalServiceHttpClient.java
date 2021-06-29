package com.example.userservice.integration;

import com.example.userservice.dto.ExternalServiceResponse;
import com.example.userservice.rabbit.message.UserCreated;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@RequiredArgsConstructor
@Component
public class ExternalServiceHttpClient {

    private final RestTemplate restTemplate;

    @Value("${external-service-url}")
    private String externalServiceUrl;

    public ExternalServiceResponse post(UserCreated userCreated) {
        RequestEntity<UserCreated> requestEntity = RequestEntity.post(URI.create(externalServiceUrl + "/post"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(userCreated);
        return restTemplate.exchange(requestEntity, ExternalServiceResponse.class)
                .getBody();
    }

}
