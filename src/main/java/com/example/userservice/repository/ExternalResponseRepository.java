package com.example.userservice.repository;

import com.example.userservice.model.ExternalResponse;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ExternalResponseRepository extends MongoRepository<ExternalResponse, String> {
}
