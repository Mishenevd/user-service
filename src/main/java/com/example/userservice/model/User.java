package com.example.userservice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Data
public class User {

    @Id
    private String id;

    private String userName;

    private String firstName;

    private String lastName;

    private LocalDateTime creationDate;

}
