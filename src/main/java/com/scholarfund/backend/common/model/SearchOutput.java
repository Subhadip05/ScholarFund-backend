package com.scholarfund.backend.common.model;

import java.util.List;

public record SearchOutput<T>(List<T> data, int page, int size, long totalRecords) {
}