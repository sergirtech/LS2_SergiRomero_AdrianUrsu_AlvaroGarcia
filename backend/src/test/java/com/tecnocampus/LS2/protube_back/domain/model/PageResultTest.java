package com.tecnocampus.LS2.protube_back.domain.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PageResultTest {
    @Test
    void hasNextWhenMoreItemsRemain() {
        assertTrue(new PageResult<>(List.of("a", "b"), 0, 2, 3).hasNext());
    }

    @Test
    void noNextOnLastPage() {
        assertFalse(new PageResult<>(List.of("c"), 1, 2, 3).hasNext());
    }
}
