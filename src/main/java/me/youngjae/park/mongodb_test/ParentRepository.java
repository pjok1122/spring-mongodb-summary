package me.youngjae.park.mongodb_test;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface ParentRepository extends CrudRepository<Parent, String> {

    List<Parent> findByName(String name);
    List<Parent> findByChildName(String name);

    Page<Parent> findPageByName(String name, Pageable pageable);
}
