package me.youngjae.park.mongodb_test;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "parent")
public class Parent {

    public Parent(String name, int age, Child child) {
        this.name = name;
        this.age = age;
        this.child = child;
    }

    @Id
    private String id;

    private String name;
    private int age;
    private Child child;
}
