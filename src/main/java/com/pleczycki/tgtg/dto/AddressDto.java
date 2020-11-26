package com.pleczycki.tgtg.dto;

import lombok.Data;

@Data
public class AddressDto {
    private String street;
    private String buildingNo;
    private String city;
    private double latitude;
    private double longitude;
}
