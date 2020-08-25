package me.youngjae.park.mongodb_test.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import me.youngjae.park.mongodb_test.entity.Child;
import me.youngjae.park.mongodb_test.entity.Parent;
import me.youngjae.park.mongodb_test.repository.ChildRepository;
import me.youngjae.park.mongodb_test.repository.ParentRepository;

@SpringBootTest
class ParentQueryDslServiceTest {

    @Autowired
    ParentQueryDslService parentQueryDslService;

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    ChildRepository childRepository;

    @Autowired
    ParentRepository parentRepository;

    @BeforeEach
    public void initTestData() {
        mongoTemplate.dropCollection(Parent.class);
        mongoTemplate.dropCollection(Child.class);

        Child child = new Child("child", 3);
        Parent parent = new Parent("parent", 32, child);

        Child child2 = new Child("child2", 3);
        Parent parent2 = new Parent("parent2", 32, child2);

        Child child3 = new Child("child2", 3);
        Parent parent3 = new Parent("parent2", 32, child3);

        childRepository.saveAll(Arrays.asList(child, child2, child3));
        parentRepository.saveAll(Arrays.asList(parent, parent2, parent3));
    }

    @Test
    void usingQueryDslExecutor() {
        List<Parent> parents = parentQueryDslService.findParentByQueryDsl("parent2", "child");

        for (Parent parent : parents) {
            System.out.println(parent);
        }
    }

}