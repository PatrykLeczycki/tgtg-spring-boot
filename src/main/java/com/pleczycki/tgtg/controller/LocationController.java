package com.pleczycki.tgtg.controller;

import com.pleczycki.tgtg.dto.LocationDto;
import com.pleczycki.tgtg.dto.ReviewDto;
import com.pleczycki.tgtg.model.Location;
import com.pleczycki.tgtg.model.Review;
import com.pleczycki.tgtg.service.LocationService;
import com.pleczycki.tgtg.utils.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import javax.validation.Valid;

@RestController
@Slf4j
@RequestMapping("/location")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @PostMapping("/add")
    public ApiResponse addLocation(@Valid @RequestBody LocationDto locationDto) {
        return locationService.addLocation(locationDto);
    }

    @PostMapping("/edit/{id}")
    public ApiResponse updateReview(@RequestBody LocationDto locationDto,
                                    @PathVariable("id") Long locationId) {
        return locationService.updateLocation(locationDto, locationId);
    }

    @GetMapping("/all")
    public List<Location> getAll() {
        return locationService.getAll();
    }

    @GetMapping("/map")
    public List<Location> getMap() {
        return locationService.getAll();
    }

    @GetMapping("/map/latest")
    public List<Location> getLatestInCities() {
        return locationService.getLatestInCities();
    }

    @GetMapping("/all/{city}")
    public List<Location> getAllByCity(@PathVariable String city) {
        return locationService.getAllByCity(city);
    }

    @GetMapping("/{id}")
    public Location getReview(@PathVariable Long id) {
        return locationService.getLocation(id);
    }
}
