package me.youngjae.park.mongodb_test;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

@Component
public class AppRunner implements ApplicationRunner {

    @Autowired
    MongoTemplate template;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Drop Collection
        template.dropCollection(Account.class);

        // Insert
        Account account1 = new Account("youngjae park", "youngjae.p");
        Account account2 = new Account("youngjae p", "youngjae.p");
        Account account3 = new Account("youngjae", "youngjae.park");

        template.insert(account1);
        template.insert(account2);
        template.insert(account3);

        // Find
        Account findAccount = template.findById(account1.getId(), Account.class);

        // Update
        template.updateFirst(Query.query(Criteria.where("username").is("youngjae park")),
                             Update.update("email", "NONE"), Account.class);

        // Delete
        template.remove(account2);

        // find All
        template.findAll(Account.class).forEach(System.out::println);

    }
}
