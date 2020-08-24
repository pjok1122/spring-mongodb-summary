package me.youngjae.park.mongodb_test;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "child")
public class Child {
    @Id
    private String id;
    private String name;
    private int age;

    public Child(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
