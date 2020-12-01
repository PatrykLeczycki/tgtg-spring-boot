package com.pleczycki.tgtg.service;

import com.pleczycki.tgtg.model.Address;
import com.pleczycki.tgtg.model.BlacklistLocationDto;
import com.pleczycki.tgtg.model.Location;
import com.pleczycki.tgtg.model.Role;
import com.pleczycki.tgtg.model.RoleName;
import com.pleczycki.tgtg.model.User;
import com.pleczycki.tgtg.repository.LocationRepository;
import com.pleczycki.tgtg.repository.UserRepository;
import com.pleczycki.tgtg.utils.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.apache.commons.lang3.time.DateUtils.addDays;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BlacklistServiceTest {

    @InjectMocks
    private BlacklistService blacklistService;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private UserRepository userRepository;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private static List<Location> allLocations;
    private static List<Location> distinctBlacklist;
    private static List<Location> blacklistWithoutCounts;
    private static Role userRole;
    private static User user;

    @BeforeEach
    void setup() throws ParseException {
        MockitoAnnotations.initMocks(this);
        userRole = initializeUserRole();
        allLocations = getLocationList();
        blacklistWithoutCounts = initializeBlacklist();
        distinctBlacklist = initializeDistinctBlacklist();
        user = initializeUser();
    }

    private static User initializeUser() {
        final User user = new User();

        user.setId(1L);
        user.setUsername("");
        user.setEmail("email@email.com");
        user.setPassword("password");
        user.setRoles(Set.of(userRole));
        user.setEnabled(false);
        user.setRegistrationToken("registrationToken");
        user.setPassRecoveryToken("passRecoveryToken");
        user.setReviews(new ArrayList<>());
        user.setCreatedAt(new Date());
        user.setLocationsBlacklist(new LinkedList<>(List.of(
                allLocations.get(0),
                allLocations.get(1),
                allLocations.get(3)
        )));

        return user;
    }

    private Role initializeUserRole() {
        final Role role = new Role();

        role.setId(1L);
        role.setName(RoleName.ROLE_USER);

        return role;
    }

    private static List<Location> getLocationList() throws ParseException {
        List<Location> locations = new ArrayList<>();
        String dateInString = "10-01-2020";
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date date = formatter.parse(dateInString);
        for (int i = 1; i <= 10; i++) {

            final Location location = new Location();
            Address address = new Address();

            address.setId((long) i);
            address.setStreet("Ulica" + i);
            address.setBuildingNo("" + i);
            address.setCity("Miasto" + (i % 3));
            address.setLatitude(52.165657);
            address.setLongitude(22.283413);

            location.setId((long) i);
            location.setAddress(address);
            location.setName("Nazwa" + i);
            location.setRating((i % 5) + 1);
            location.setCreatedAt(addDays(date, i));
            location.setModifiedAt(addDays(date, i));

            locations.add(location);
        }
        return locations;
    }

    private static List<Location> initializeDistinctBlacklist() {
        return new LinkedList<>(List.of(
                allLocations.get(0),
                allLocations.get(1),
                allLocations.get(3),
                allLocations.get(4),
                allLocations.get(7),
                allLocations.get(8),
                allLocations.get(9))
        );
    }

    private static List<Location> initializeBlacklist() {
        return new LinkedList<>(List.of(
                allLocations.get(0), allLocations.get(0), allLocations.get(0),
                allLocations.get(1),
                allLocations.get(3),
                allLocations.get(4), allLocations.get(4),
                allLocations.get(7),
                allLocations.get(8), allLocations.get(8), allLocations.get(8),
                allLocations.get(9))
        );
    }

    @Test
    void shouldReturnBlacklist() {
        //given
        when(locationRepository.getDistinctBlacklist()).thenReturn(distinctBlacklist);
        when(locationRepository.getBlacklist()).thenReturn(blacklistWithoutCounts);

        List<BlacklistLocationDto> blacklistWithCounts = new ArrayList<>();
        distinctBlacklist.forEach(location -> {
            blacklistWithCounts.add(new BlacklistLocationDto(location, Collections.frequency(blacklistWithoutCounts, location)));
        });

        //when
        ResponseEntity<List<BlacklistLocationDto>> blacklistResponseEntity = blacklistService.getBlacklist();

        //then
        assertAll(() -> {
            assertThat(blacklistResponseEntity.getStatusCode(), is(HttpStatus.OK));
            assertNotNull(blacklistResponseEntity.getBody());
            assertThat(blacklistResponseEntity.getBody(), is(blacklistWithCounts));
            assertThat(blacklistResponseEntity.getBody().get(0).getCount(), is(Collections.frequency(blacklistWithoutCounts, distinctBlacklist.get(0))));
        });
    }

    @Test
    void shouldAddLocationToBlacklist() {
        //given
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(allLocations.get(2)));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        Map<String, String> locationBlacklistData = new HashMap<>();
        locationBlacklistData.put("userId", "1");
        locationBlacklistData.put("locationId", "1");

        //when
        ResponseEntity<ApiResponse> addToBlacklistResponseEntity = blacklistService.addToBlacklist(locationBlacklistData);

        //then
        verify(userRepository, times(1)).save(userCaptor.capture());
        assertAll(() -> {
            User user = userCaptor.getAllValues().get(0);
            assertNotNull(user);
            assertFalse(user.getLocationsBlacklist().isEmpty());
            assertThat(user.getLocationsBlacklist(), contains(allLocations.get(0), allLocations.get(1), allLocations.get(3), allLocations.get(2)));
        });

    }

    @Test
    void shouldRespondWithBadRequestStatusWhenAddingInvalidLocationToBlacklist() {
        //given
        when(locationRepository.findById(anyLong())).thenReturn(Optional.empty());
        Map<String, String> locationBlacklistData = new HashMap<>();
        locationBlacklistData.put("userId", "1");
        locationBlacklistData.put("locationId", "1");

        //when
        ResponseEntity<ApiResponse> addToBlacklistResponseEntity = blacklistService.addToBlacklist(locationBlacklistData);

        //then
        assertAll(() -> {
            assertThat(addToBlacklistResponseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
            assertNotNull(addToBlacklistResponseEntity.getBody());
            assertFalse(addToBlacklistResponseEntity.getBody().getSuccess());
            assertThat(addToBlacklistResponseEntity.getBody().getMessage(), is("Location ID not found in database."));
        });
    }

    @Test
    void shouldRespondWithBadRequestStatusWhenAddingLocationToBlacklistOfInvalidUser() {
        //given
        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(allLocations.get(0)));
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        Map<String, String> locationBlacklistData = new HashMap<>();
        locationBlacklistData.put("userId", "1");
        locationBlacklistData.put("locationId", "1");

        //when
        ResponseEntity<ApiResponse> addToBlacklistResponseEntity = blacklistService.addToBlacklist(locationBlacklistData);

        //then
        assertAll(() -> {
            assertThat(addToBlacklistResponseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
            assertNotNull(addToBlacklistResponseEntity.getBody());
            assertFalse(addToBlacklistResponseEntity.getBody().getSuccess());
            assertThat(addToBlacklistResponseEntity.getBody().getMessage(), is("User ID not found in database."));
        });
    }
}