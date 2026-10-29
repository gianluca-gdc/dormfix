package com.dormfix.category;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Category endpoints. Categories are fixed seed data, so this is read-only. */
@RestController
@RequestMapping("/api")
public class CategoryController {

    private final CategoryRepository categories;

    // Spring sees this constructor and passes in the repository for us (dependency injection).
    public CategoryController(CategoryRepository categories) {
        this.categories = categories;
    }

    /** GET /api/categories - every category as { id, name }. */
    @GetMapping("/categories")
    public List<CategoryResponse> getCategories() {
        return categories.findAll().stream()
                .map(CategoryResponse::from)
                .toList();
    }
}
