package com.pleczycki.tgtg.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AddressTest {

    private static Address address;

    @BeforeEach
    void setup() {
        address = initializeAddress();
    }

    private static Address initializeAddress() {
        Address tempAddress = new Address();
        tempAddress.setId(1L);
        tempAddress.setStreet("Street");
        tempAddress.setBuildingNo("123");
        tempAddress.setCity("City");
        tempAddress.setLatitude(52.2347399);
        tempAddress.setLongitude(21.0094018);

        return tempAddress;
    }

    @Test
    void clonedAddressShouldBeEqual() {
        //given
        Address newAddress = new Address(address);

        //when
        //then
        assertEquals(address, newAddress);
    }
}