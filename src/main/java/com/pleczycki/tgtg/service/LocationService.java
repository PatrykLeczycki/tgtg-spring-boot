package com.pleczycki.tgtg.service;

import com.pleczycki.tgtg.dto.LocationDto;
import com.pleczycki.tgtg.model.Address;
import com.pleczycki.tgtg.model.Location;
import com.pleczycki.tgtg.model.Review;
import com.pleczycki.tgtg.model.User;
import com.pleczycki.tgtg.repository.AddressRepository;
import com.pleczycki.tgtg.repository.LocationRepository;
import com.pleczycki.tgtg.repository.UserRepository;
import com.pleczycki.tgtg.utils.ApiResponse;
import com.pleczycki.tgtg.utils.CustomModelMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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
    private UserRepository userRepository;

    public ApiResponse addLocation(LocationDto locationDto) {
        Address unfetchedAddress = locationDto.getAddress();
        Address fetchedAddress = addressRepository.save(unfetchedAddress);
        locationDto.setAddress(fetchedAddress);
        Location location = mapper.map(locationDto, Location.class);
        location.setCreatedAt(new Date());
        Location savedLocation = locationRepository.save(location);
        return new ApiResponse(true, "" + savedLocation.getId());
    }

    public List<Location> getAll() {
        return locationRepository.findAll();
    }

    public Location getLocation(Long id) {
        Optional<Location> location = locationRepository.findById(id);
        return location.orElse(null);
    }

    public List<Location> getAllByCity(String city) {
        return locationRepository.getAllByCity(city);
    }

    public ApiResponse updateLocation(LocationDto locationDto, Long locationId) {
        Location location = locationRepository.getOne(locationId);

        Long addressId = location.getAddress().getId();
        Address address = addressRepository.getOne(addressId);
        address.setId(addressId);
        address.setBuildingNo(locationDto.getAddress().getBuildingNo());
        address.setStreet(locationDto.getAddress().getStreet());
        address.setCity(locationDto.getAddress().getCity());
        addressRepository.save(address);
        location.setAddress(address);
        Location savedLocation = locationRepository.save(location);

        return new ApiResponse(true, "" + savedLocation.getId());
    }

    public List<Location> getLatestInCities() {
        List<String> distinctCities = addressRepository.getDistinctCities();
        List<Location> latestLocations = new LinkedList<>();
        distinctCities
                .forEach(city -> latestLocations.addAll(locationRepository.getLatestLocationsByCity(city)));
        return latestLocations;
    }
}
