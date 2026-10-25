package com.backend.services;

import org.springframework.stereotype.Service;

import com.backend.repositories.CategoryRepository;

@Service 
public class CategoryService {
    
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
}
