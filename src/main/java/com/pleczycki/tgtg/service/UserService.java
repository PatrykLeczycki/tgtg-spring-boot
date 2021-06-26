package com.pleczycki.tgtg.service;

import com.pleczycki.tgtg.exception.ResourceNotFoundException;
import com.pleczycki.tgtg.model.Location;
import com.pleczycki.tgtg.model.Review;
import com.pleczycki.tgtg.model.User;
import com.pleczycki.tgtg.repository.LocationRepository;
import com.pleczycki.tgtg.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service("UserService")
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LocationRepository locationRepository;

    public void addReview(long userId, Review review) {
        Optional<User> optionalUser = userRepository.findById(userId);

        if(optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }

        User user = optionalUser.get();

        List<Review> userReviews = user.getReviews();
        userReviews.add(review);
        user.setReviews(userReviews);
        userRepository.save(user);
    }

    public void addLocation(long userId, Location location) {
        Optional<User> optionalUser = userRepository.findById(userId);

        if(optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }

        User user = optionalUser.get();

        List<Location> userLocations = user.getLocations();
        userLocations.add(location);
        user.setLocations(userLocations);
        userRepository.save(user);
    }

    public ResponseEntity<List<Location>> getUserBlacklist(long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }

        User user = optionalUser.get();

        return ResponseEntity.ok(user.getLocationsBlacklist());
    }

    public void deleteFromUserBlacklist(Map<String, String> data) {
        Long userId = Long.valueOf(data.get("userId"));
        Long locationId = Long.valueOf(data.get("locationId"));

        Optional<User> optionalUser = userRepository.findById(userId);

        if(optionalUser.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }

        Optional<Location> optionalLocation = locationRepository.findById(locationId);

        if (optionalLocation.isEmpty()) {
            throw new ResourceNotFoundException("Location not found");
        }

        Location location = optionalLocation.get();
        User user = optionalUser.get();

        user.getLocationsBlacklist().remove(location);
        userRepository.save(user);
    }
}
