package com.pleczycki.tgtg.dto;

import com.pleczycki.tgtg.model.Address;
import lombok.Data;

@Data
public class LocationDto {
    private String name;
    private Address address;
    private double rating;
}
