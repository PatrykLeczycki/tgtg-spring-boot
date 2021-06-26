package com.pleczycki.tgtg.controller;

import com.pleczycki.tgtg.dto.LocationDto;
import com.pleczycki.tgtg.model.Location;
import com.pleczycki.tgtg.service.LocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/location")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @PostMapping("/add")
    public ResponseEntity<Location> addLocation(@RequestPart("location") LocationDto locationDto, @RequestPart("id") Long userId) {
        return locationService.addLocation(locationDto, userId);
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

    @GetMapping("/all/user/{id}")
    public ResponseEntity<List<Location>> getAllLocationsByUserId(@PathVariable("id") Long id) {
        return locationService.getAllLocationsByUserId(id);
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

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable("id") Long locationId, @RequestBody String userId) {
        locationService.delete(locationId, userId);
    }
}
