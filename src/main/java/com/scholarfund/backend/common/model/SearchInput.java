package com.scholarfund.backend.common.model;

import org.springframework.data.domain.Sort.Direction;

public record SearchInput<T>(T filter, Integer page, Integer size, Direction sortDirection, String sortBy) {

    public SearchInput(T filter, Integer page, Integer size, Direction sortDirection, String sortBy) {
        this.filter = filter;
        this.page = page == null ? 0 : calculatePage(page);
        this.size = size == null ? Integer.MAX_VALUE : size;
        this.sortDirection = sortDirection == null ? Direction.ASC : sortDirection;
        this.sortBy = sortBy;
    }

    private Integer calculatePage(Integer page) {
        var newPage = page - 1;
        return Math.max(newPage, 0);
    }
}