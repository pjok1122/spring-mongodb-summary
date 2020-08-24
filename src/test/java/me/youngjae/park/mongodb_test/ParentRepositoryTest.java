package me.youngjae.park.mongodb_test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootTest
class ParentRepositoryTest {

    @Autowired
    ParentRepository parentRepository;

    @Autowired
    ChildRepository childRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    @Test
    void findByName() {
        initTestData();

        // 메서드 이름 규칙에 따라 Proxy에 기능을 구현해줌.
        List<Parent> parents = parentRepository.findByName("parent2");

        assertEquals(2, parents.size());
    }

    @Test
    void findByChildName() {
        initTestData();

        List<Parent> parents = parentRepository.findByChildName("child2");

        assertEquals(2, parents.size());

    }

    @Test
    void findPageByName() {
        initTestData();

        Page<Parent> page = parentRepository.findPageByName("parent2", PageRequest.of(0, 10));

        assertEquals(2, page.getTotalElements());
        assertEquals(2, page.getContent().size());
    }

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
}