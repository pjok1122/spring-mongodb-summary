package me.youngjae.park.mongodb_test;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParentService {

    private final ParentRepository parentRepository;

    public List<Parent> getAllParentByName(String name) {
        return parentRepository.findByName(name);
    }

    public Iterable<Parent> getAll() {
        return parentRepository.findAll();

    }
}
