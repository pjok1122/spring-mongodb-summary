package me.youngjae.park.mongodb_test.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;

import me.youngjae.park.mongodb_test.entity.Child;
import me.youngjae.park.mongodb_test.entity.Parent;

@SpringBootTest
class ParentMongoRepositoryTest {

    @Autowired
    ParentMongoRepository parentMongoRepository;

    @Autowired
    ChildMongoRepository childMongoRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    @Test
    void findAllPaging() {
        initTestData();

        Page<Parent> parents = parentMongoRepository.findAll(PageRequest.of(0, 10));

        assertEquals(3, parents.getTotalElements());
        assertTrue(parents.isFirst());
    }

//    @Test
//    void addQueryDSL() {
//    }

    public void initTestData() {
        mongoTemplate.dropCollection(Parent.class);
        mongoTemplate.dropCollection(Child.class);

        Child child = new Child("child", 3);
        Point point = new Point(43.6, 43.7);
        Parent parent = new Parent("parent", 32, point, child);

        Child child2 = new Child("child2", 3);
        Point point2 = new Point(50.6, 70.7);
        Parent parent2 = new Parent("parent2", 32, point2, child2);

        Child child3 = new Child("child2", 3);
        Point point3 = new Point(127.5, 128.2);
        Parent parent3 = new Parent("parent2", 32, point3, child3);

        childMongoRepository.saveAll(Arrays.asList(child, child2, child3));
        parentMongoRepository.saveAll(Arrays.asList(parent, parent2, parent3));
    }
}