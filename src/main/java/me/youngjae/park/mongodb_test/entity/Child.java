package me.youngjae.park.mongodb_test.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Document(collection = "child")
@NoArgsConstructor
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
