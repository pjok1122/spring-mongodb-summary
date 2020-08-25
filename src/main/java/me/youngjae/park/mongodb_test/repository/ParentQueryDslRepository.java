package me.youngjae.park.mongodb_test.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import me.youngjae.park.mongodb_test.entity.Parent;

public interface ParentQueryDslRepository extends MongoRepository<Parent, String>, QuerydslPredicateExecutor<Parent>, ParentQueryDslRepositoryCustom {

}
