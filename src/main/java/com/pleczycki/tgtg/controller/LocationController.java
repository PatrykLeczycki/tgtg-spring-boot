package com.pleczycki.tgtg.controller;

import com.pleczycki.tgtg.dto.LocationDto;
import com.pleczycki.tgtg.model.Location;
import com.pleczycki.tgtg.service.LocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import javax.validation.Valid;

@RestController
@Slf4j
@RequestMapping("/location")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @PostMapping("/add")
    public ResponseEntity<Location> addLocation(@Valid @RequestBody LocationDto locationDto) {
        return locationService.addLocation(locationDto);
    }

    @PostMapping("/edit/{id}")
    public ResponseEntity<Location> updateReview(@RequestBody LocationDto locationDto,
                                    @PathVariable("id") Long locationId) {
        return locationService.updateLocation(locationDto, locationId);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Location>> getAll() {
        return locationService.getAll();
    }

    @GetMapping("/map")
    public ResponseEntity<List<Location>> getMap() {
        return locationService.getAll();
    }

    @GetMapping("/map/latest")
    public ResponseEntity<List<Location>> getLatestInCities() {
        return locationService.getLatestInCities();
    }

    @GetMapping("/all/{city}")
    public ResponseEntity<List<Location>> getAllByCity(@PathVariable String city) {
        return locationService.getAllByCity(city);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<Location> getReview(@PathVariable Long id) {
        return locationService.getLocation(id);
    }
}
