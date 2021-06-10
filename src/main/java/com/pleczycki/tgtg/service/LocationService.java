package com.pleczycki.tgtg.service;

import com.pleczycki.tgtg.exception.ResourceNotFoundException;
import com.pleczycki.tgtg.exception.ResourceAlreadyExists;
import com.pleczycki.tgtg.dto.LocationDto;
import com.pleczycki.tgtg.model.Address;
import com.pleczycki.tgtg.model.Location;
import com.pleczycki.tgtg.repository.AddressRepository;
import com.pleczycki.tgtg.repository.LocationRepository;
import com.pleczycki.tgtg.utils.CustomModelMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service("LocationService")
public class LocationService {

    @Autowired
    private CustomModelMapper mapper;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public ResponseEntity<Location> addLocation(LocationDto locationDto, long userId) {
        Address unfetchedAddress = locationDto.getAddress();
        Address fetchedAddress = addressRepository.save(unfetchedAddress);
        locationDto.setAddress(fetchedAddress);
        Location location = mapper.map(locationDto, Location.class);
        location.setCreatedAt(new Date());

        Optional<Location> existingLocation = locationRepository.findAll().stream().
                filter(loc -> loc.equals(location)).
                findFirst();

        if (existingLocation.isPresent()) {
            throw new ResourceAlreadyExists("Location already exists");
        } else {
            Location savedLocation = locationRepository.save(location);
            userService.addLocation(userId, savedLocation);
            return ResponseEntity.ok(savedLocation);
        }
    }

    public ResponseEntity<List<Location>> getAll() {
        List<Location> locations = locationRepository.findAll();
        return ResponseEntity.ok(locations);
    }

    public ResponseEntity<List<Location>> getAllLocationsByUserId(Long userId) {
        List<Location> locations = locationRepository.findAllByUserId(userId);
        return ResponseEntity.ok(locations);
    }

    public ResponseEntity<Location> getLocation(Long id) {
        Optional<Location> location = locationRepository.findById(id);

        if (location.isEmpty()) {
            throw new ResourceNotFoundException("Location not found");
        }

        return ResponseEntity.of(location);
    }

    public ResponseEntity<List<Location>> getAllByCity(String city) {
        List<Location> locations = locationRepository.findAll().stream()
                .filter(location -> location.getAddress().getCity().equals(city)).collect(
                        Collectors.toList());
        return ResponseEntity.ok(locations);
    }

    public ResponseEntity<Location> updateLocation(LocationDto locationDto, Long locationId) {
        Optional<Location> optionalLocation = locationRepository.findById(locationId);

        if (optionalLocation.isEmpty()) {
            throw new ResourceNotFoundException("Location not found");
        }

        Location location = optionalLocation.get();

        Long addressId = location.getAddress().getId();
        Optional<Address> optionalAddress = addressRepository.findById(addressId);

        if (optionalAddress.isEmpty()) {
            throw new ResourceNotFoundException("Address not found");
        }

        Address address = optionalAddress.get();

        address.setId(addressId);
        address.setBuildingNo(locationDto.getAddress().getBuildingNo());
        address.setStreet(locationDto.getAddress().getStreet());
        address.setCity(locationDto.getAddress().getCity());
        address.setLatitude(locationDto.getAddress().getLatitude());
        address.setLongitude(locationDto.getAddress().getLongitude());
        addressRepository.save(address);

        location.setName(locationDto.getName());
        location.setAddress(address);
        location.setModifiedAt(new Date());
        Location savedLocation = locationRepository.save(location);

        return ResponseEntity.ok(savedLocation);
    }

    public ResponseEntity<List<Location>> getLatestInCities() {

        List<String> distinctCities = addressRepository.getDistinctCities();
        List<Location> latestLocations = new LinkedList<>();
        distinctCities
                .forEach(city -> latestLocations.addAll(locationRepository.getLatestLocationsByCity(city)));
        return ResponseEntity.ok(latestLocations);
    }

    void updateRating(long locationId) {

        Optional<Location> optionalLocation = locationRepository.findById(locationId);

        if(optionalLocation.isEmpty()) {
            throw new ResourceNotFoundException("Location not found");
        }

        Location location = optionalLocation.get();
        location.setRating(locationRepository.getAverageRating(locationId));
        locationRepository.save(location);
    }
}
