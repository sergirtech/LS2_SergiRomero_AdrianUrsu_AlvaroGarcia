package com.tecnocampus.LS2.protube_back.domain.model;

import java.util.List;

/** One page of results. Plain Java so the application layer doesn't depend on Spring Data. */
public record PageResult<T>(List<T> items, int page, int size, long totalItems) {

    public PageResult {
        items = List.copyOf(items);
    }

    public boolean hasNext() {
        return (long) (page + 1) * size < totalItems;
    }
}
