package com.pleczycki.tgtg.service;

import com.pleczycki.tgtg.model.BlacklistLocationDto;
import com.pleczycki.tgtg.model.Location;
import com.pleczycki.tgtg.model.User;
import com.pleczycki.tgtg.repository.LocationRepository;
import com.pleczycki.tgtg.repository.UserRepository;
import com.pleczycki.tgtg.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service("BlacklistService")
public class BlacklistService {

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private UserRepository userRepository;

    public ApiResponse addToBlacklist(Map<String, String> locationBlacklistData) {
        Long userId = Long.valueOf(locationBlacklistData.get("userId"));
        Long locationId = Long.valueOf(locationBlacklistData.get("locationId"));
        Location location = locationRepository.getOne(locationId);
        User user = userRepository.getOne(userId);

        user.getLocationsBlacklist().add(location);
        userRepository.save(user);
        return new ApiResponse(true, "Location successfully added to blacklist.");
    }

    public List<BlacklistLocationDto> getBlacklist() {
        List<Location> blacklist = locationRepository.getBlacklist();
        List<BlacklistLocationDto> completeBlacklist = new ArrayList<>();
        blacklist.forEach(location -> {
            int occurences = locationRepository.countLocationOnBlacklist(location.getId());
            completeBlacklist.add(new BlacklistLocationDto(location, occurences));
        });
        return completeBlacklist;
    }
}
