package com.pleczycki.tgtg.service;

import com.pleczycki.tgtg.model.Location;
import com.pleczycki.tgtg.model.User;
import com.pleczycki.tgtg.repository.LocationRepository;
import com.pleczycki.tgtg.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service("UserService")
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LocationRepository locationRepository;

    public List<Location> getUserBlacklist(long userId) {
        User user = userRepository.getOne(userId);
        return user.getLocationsBlacklist();
    }

    public void deleteFromUserBlacklist(Map<String, String> data) {
        Long userId = Long.valueOf(data.get("userId"));
        Long locationId = Long.valueOf(data.get("locationId"));
        User user = userRepository.getOne(userId);
        Location location = locationRepository.getOne(locationId);
        user.getLocationsBlacklist().remove(location);
        userRepository.save(user);
    }
}
