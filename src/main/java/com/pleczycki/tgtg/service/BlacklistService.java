package com.pleczycki.tgtg.service;

import com.pleczycki.tgtg.dto.BlacklistLocationDto;
import com.pleczycki.tgtg.model.Location;
import com.pleczycki.tgtg.model.User;
import com.pleczycki.tgtg.repository.LocationRepository;
import com.pleczycki.tgtg.repository.UserRepository;
import com.pleczycki.tgtg.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service("BlacklistService")
public class BlacklistService {

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<ApiResponse> addToBlacklist(Map<String, String> locationBlacklistData) {
        Long userId = Long.valueOf(locationBlacklistData.get("userId"));
        Long locationId = Long.valueOf(locationBlacklistData.get("locationId"));
        Optional<Location> optionalLocation = locationRepository.findById(locationId);

        if (optionalLocation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Location ID not found in database."));
        }

        Optional<User> optionalUser = userRepository.findById(userId);

        if(optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "User ID not found in database."));
        }

        Location location = optionalLocation.get();
        User user = optionalUser.get();

        user.getLocationsBlacklist().add(location);
        userRepository.save(user);
        return ResponseEntity.ok(new ApiResponse(true, "Location successfully added to blacklist."));
    }

    public ResponseEntity<List<BlacklistLocationDto>> getBlacklist() {
        List<Location> distinctBlacklist = locationRepository.getDistinctBlacklist();
        List<Location> blacklistWithDuplicates = locationRepository.getBlacklist();

        List<BlacklistLocationDto> blacklistWithCounts = new ArrayList<>();
        distinctBlacklist.forEach(location -> {
            int occurences = Collections.frequency(blacklistWithDuplicates, location);
            blacklistWithCounts.add(new BlacklistLocationDto(location, occurences));
        });
        return ResponseEntity.ok(blacklistWithCounts);
    }
}
