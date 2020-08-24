package me.youngjae.park.mongodb_test;

import org.springframework.data.repository.CrudRepository;

public interface ChildRepository extends CrudRepository<Child, String> {
}
