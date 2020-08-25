package me.youngjae.park.mongodb_test.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Document(collection = "parent")
@NoArgsConstructor
public class Parent {

    public Parent(String name, int age, Child child) {
        this.name = name;
        this.age = age;
        this.child = child;
    }

    public Parent(String name, int age, Point position, Child child) {
        this(name, age, child);
        this.position = position;
    }


    @Id
    private String id;

    private String name;
    private int age;
    private Point position;
    private Child child;


}
