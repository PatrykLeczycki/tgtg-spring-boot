package com.pleczycki.tgtg.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LocationTest {
    private static Location location;

    @BeforeEach
    void setup() {
        location = initializeLocation();
    }

    private static Location initializeLocation() {
        Location location = new Location();
        Address tempAddress = new Address();

        tempAddress.setId(1L);
        tempAddress.setStreet("Street");
        tempAddress.setBuildingNo("123");
        tempAddress.setCity("City");
        tempAddress.setLatitude(52.2347399);
        tempAddress.setLongitude(21.0094018);

        location.setId(1L);
        location.setAddress(tempAddress);
        location.setName("locationName");
        location.setRating(5.0);

        return location;
    }

    @Test
    void clonedLocationShouldBeEqual() {
        //given
        Location newLocation = new Location(location);

        //when
        //then
        assertEquals(location, newLocation);
    }
}