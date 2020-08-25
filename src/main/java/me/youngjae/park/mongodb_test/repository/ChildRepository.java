package me.youngjae.park.mongodb_test.repository;

import org.springframework.data.repository.CrudRepository;

import me.youngjae.park.mongodb_test.entity.Child;

public interface ChildRepository extends CrudRepository<Child, String> {
}
