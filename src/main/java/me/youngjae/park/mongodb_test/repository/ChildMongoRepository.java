package me.youngjae.park.mongodb_test.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import me.youngjae.park.mongodb_test.entity.Child;

public interface ChildMongoRepository extends MongoRepository<Child, String> {
}
