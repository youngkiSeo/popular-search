package com.example.redis.search.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchVo {
    private String product;
    private Double count;
}
