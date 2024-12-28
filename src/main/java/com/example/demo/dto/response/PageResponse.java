package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PageResponse<T> {
    private int pageNo;
    private int pageSize;
    private int totalPages;
    private long totalElements;
    private T contents;
}
