package me.youngjae.park.mongodb_test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@SpringBootTest
public class MongoTest {

    @Autowired
    MongoTemplate mongoTemplate;

    @Test
    void saveCascading() {
        Child child = new Child("child", 3);
        Parent parent = new Parent("parent", 32, child);

        mongoTemplate.save(parent);
    }

    @Test
    void findAndModify() {

        mongoTemplate.dropCollection(Parent.class);

        Child child = new Child("child", 3);
        Parent parent = new Parent("parent", 32, child);

        mongoTemplate.save(parent);

        // return OldValue
        Parent oldParent = mongoTemplate.findAndModify(Query.query(Criteria.where("name").is("parent")),
                                                       new Update().inc("age", 1), Parent.class);

        assertEquals(32, oldParent.getAge());
        assertEquals(3, oldParent.getChild().getAge());

        // return newValue
        Parent findParent =  mongoTemplate.findOne(Query.query(Criteria.where("name").is("parent")), Parent.class);

        assertEquals(33, findParent.getAge());
        assertEquals(3, findParent.getChild().getAge());
    }

    @Test
    void queryDocumentsInACollection() {
        mongoTemplate.dropCollection(Parent.class);

        Child child = new Child("child", 3);
        Parent parent = new Parent("parent", 32, child);

        Child child2 = new Child("child2", 3);
        Parent parent2 = new Parent("parent2", 32, child2);

        Child child3 = new Child("child2", 3);
        Parent parent3 = new Parent("parent2", 32, child3);

        mongoTemplate.save(parent);
        mongoTemplate.save(parent2);
        mongoTemplate.save(parent3);    //same content with parent2

        List<String> result = mongoTemplate.query(Parent.class)
                                           .distinct("name")
                                           .matching(Query.query(Criteria.where("age").gt(30)))
                                           .as(String.class)
                                           .all();

        assertEquals(2, result.size());
        assertEquals("parent", result.get(0));
        assertEquals("parent2", result.get(1));
    }
}
