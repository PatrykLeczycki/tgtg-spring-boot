package com.pleczycki.tgtg.controller;

import com.pleczycki.tgtg.model.BlacklistLocationDto;
import com.pleczycki.tgtg.model.Location;
import com.pleczycki.tgtg.repository.LocationRepository;
import com.pleczycki.tgtg.service.BlacklistService;
import com.pleczycki.tgtg.service.UserService;
import com.pleczycki.tgtg.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/blacklist")
@RequiredArgsConstructor
public class BlacklistController {

    @Autowired
    private BlacklistService blacklistService;

    @Autowired
    private UserService userService;

    @Autowired
    private LocationRepository locationRepository;

    @PostMapping("/add")
    public ApiResponse addToBlacklist(@RequestBody Map<String, String> locationBlacklistData) {
        return blacklistService.addToBlacklist(locationBlacklistData);
    }

    @GetMapping("/all")
    public List<BlacklistLocationDto> getBlacklist() {
        return blacklistService.getBlacklist();
    }

    @GetMapping("/user/{id}")
    public List<Location> getUserBlacklist(@PathVariable long id) {
        List<Location> userBlacklist = userService.getUserBlacklist(id);
        return userBlacklist;
    }

    @GetMapping("/count/{id}")
    public int countLocationOnBlacklist(@PathVariable("id") long locationId) {
        return locationRepository.countLocationOnBlacklist(locationId);
    }


    @PostMapping("/delete")
    public void deleteFromUserBlacklist(@RequestBody Map<String, String> data) {
        userService.deleteFromUserBlacklist(data);
    }
}
