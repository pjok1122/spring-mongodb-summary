package me.youngjae.park.mongodb_test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
public class MongodbTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(MongodbTestApplication.class, args);
    }

}
