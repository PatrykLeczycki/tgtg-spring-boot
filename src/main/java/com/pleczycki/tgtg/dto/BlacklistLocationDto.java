package com.pleczycki.tgtg.dto;

import com.pleczycki.tgtg.model.Location;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class BlacklistLocationDto {
    private Location location;
    private int count;

    public BlacklistLocationDto(Location location, int count) {
        this.location = location;
        this.count = count;
    }
}
