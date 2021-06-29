package com.example.userservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class ExternalResponse {

    @Id
    private String id;

    private String message;

}
