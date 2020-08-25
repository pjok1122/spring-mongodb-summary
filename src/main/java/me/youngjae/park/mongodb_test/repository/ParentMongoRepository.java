package me.youngjae.park.mongodb_test.repository;

import java.util.List;

import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;

import me.youngjae.park.mongodb_test.entity.Parent;

public interface ParentMongoRepository extends MongoRepository<Parent, String> {

    List<Parent> findByPositionNear(Point point, Distance distance);
}
