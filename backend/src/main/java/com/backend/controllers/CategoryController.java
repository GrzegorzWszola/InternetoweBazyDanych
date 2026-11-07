package com.backend.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.backend.api.controllers.CategoriesApi;
import com.backend.api.model.CategoryRequest;
import com.backend.api.model.CategoryResponse;
import com.backend.services.CategoryService;

import jakarta.validation.Valid;

@RestController 
public class CategoryController implements CategoriesApi {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public ResponseEntity<CategoryResponse> createCategory(@Valid CategoryRequest categoryRequest) {
        throw new UnsupportedOperationException("Unimplemented method 'createCategory'");
    }

    @Override
    public ResponseEntity<Void> deleteCategory(Integer id) {
        throw new UnsupportedOperationException("Unimplemented method 'deleteCategory'");
    }

    @Override
    public ResponseEntity<CategoryResponse> getCategory(Integer id) {
        throw new UnsupportedOperationException("Unimplemented method 'getCategory'");
    }

    @Override
    public ResponseEntity<List<CategoryResponse>> listCategories() {
        throw new UnsupportedOperationException("Unimplemented method 'listCategories'");
    }

    @Override
    public ResponseEntity<CategoryResponse> updateCategory(Integer id, @Valid CategoryRequest categoryRequest) {
        throw new UnsupportedOperationException("Unimplemented method 'updateCategory'");
    }
}
