package me.youngjae.park.mongodb_test.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;

import lombok.RequiredArgsConstructor;
import me.youngjae.park.mongodb_test.entity.Parent;
import me.youngjae.park.mongodb_test.entity.QParent;
import me.youngjae.park.mongodb_test.repository.ParentQueryDslRepository;

@Service
@RequiredArgsConstructor
public class ParentQueryDslService {

    @Autowired
    private final ParentQueryDslRepository parentRepository;

    public List<Parent> findParentByQueryDsl(String parentName, String childName) {
        QParent parent = QParent.parent;

        BooleanExpression expression = parent.name.eq(parentName)
                                                  .and(parent.child.name.startsWith(childName));

        Iterable<Parent> all = parentRepository.findAll(expression);

        List<Parent> parents = new ArrayList<>();
        all.forEach(parents::add);

        return parents;
    }
}