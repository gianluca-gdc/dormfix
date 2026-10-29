package com.dormfix.category;

/** JSON body for one category in GET /api/categories, e.g. { "id": 1, "name": "Plumbing" }. */
public record CategoryResponse(Long id, String name) {

    /** Copies the fields we want to show out of the database entity. */
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}
