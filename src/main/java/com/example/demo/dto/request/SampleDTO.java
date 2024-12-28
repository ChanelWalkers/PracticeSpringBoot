package com.example.demo.dto.request;

import lombok.Builder;

import java.io.Serializable;

@Builder
public class SampleDTO implements Serializable {
    private Integer id;
    private String name;
}
