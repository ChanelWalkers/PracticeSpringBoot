package com.example.demo.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
public class AddressDTO implements Serializable {
    private String apartmentNumber;
    private String floor;
    private String building;
    private String streetNumber;
    private String street;
    private String city;
    private String country;
    private Integer addressType;
}
