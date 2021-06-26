package com.pleczycki.tgtg.service;

import com.pleczycki.tgtg.exception.ResourceNotFoundException;
import com.pleczycki.tgtg.model.Address;
import com.pleczycki.tgtg.model.Location;
import com.pleczycki.tgtg.model.Review;
import com.pleczycki.tgtg.model.User;
import com.pleczycki.tgtg.repository.LocationRepository;
import com.pleczycki.tgtg.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LocationRepository locationRepository;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private static Review review;
    private static List<Location> locations;
    private static User user;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        locations = getExistingLocations();
        review = initializeReviewWithExistingLocation();
        user = initializeUser();
    }

    private static Review initializeReviewWithExistingLocation() {
        final Review review = new Review();

        review.setId(1L);
        review.setLocation(locations.get(0));
        review.setCreatedAt(new Date());
        review.setComment("comment");
        review.setRating(5);
        review.setStandardPrice(30);
        review.setDiscountPrice(10);
        review.setPickupTime(new Date());

        return review;
    }

    private static List<Location> getExistingLocations() {
        List<Location> existingLocations = new LinkedList<>();

        for (int i = 1; i <= 5; i++) {
            Location location = new Location();
            Address address = new Address();
            address.setId((long) i);
            address.setStreet("Ulica" + i);
            address.setBuildingNo("" + i);
            address.setCity("Miasto" + i);
            address.setLatitude(52.165657);
            address.setLongitude(22.283413);

            location.setId((long) i);
            location.setAddress(address);
            location.setName("Nazwa" + i);
            location.setRating((long) ((i % 5) + 1));
            existingLocations.add(location);
        }

        return existingLocations;
    }

    private static User initializeUser() {
        final User user = new User();

        user.setId(2L);
        user.setEmail("email@email.com");
        user.setPassword("password");
        user.setCreatedAt(new Date());
        user.setUsername(null);
        user.setRegistrationToken(null);
        user.setPassRecoveryToken(null);
        user.setEnabled(true);
        user.setReviews(new ArrayList<>());
        user.setLocationsBlacklist(new LinkedList<>(List.of(
                locations.get(0),
                locations.get(2),
                locations.get(4)))
        );

        return user;
    }

    @Test
    void shouldAddReviewToUser() {
        //given
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        int userReviewsSize = user.getReviews().size();
        //when
        userService.addReview(1, review);

        //then
        verify(userRepository, times(1)).save(userCaptor.capture());
        assertAll(() -> {
            User user1 = userCaptor.getAllValues().get(0);
            assertNotNull(user1);
            assertThat(user1, is(sameInstance(user)));
            assertThat(user1.getReviews().size(), is(userReviewsSize + 1));
        });
    }

    @Test
    void shouldThrowExceptionWhenAddingReviewToInvalidUser() {
        //given
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        //when
        //then
        assertAll(() -> {
            ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class,
                    () -> userService
                            .addReview(1, review));
            assertEquals("User not found", resourceNotFoundException.getMessage());
        });
    }

    @Test
    void shouldReturnUserLocationBlacklist() {
        //given
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        //when
        ResponseEntity<List<Location>> userBlacklistResponseEntity = userService.getUserBlacklist(user.getId());

        //then
        assertAll(() -> {
            assertNotNull(userBlacklistResponseEntity.getBody());
            assertThat(userBlacklistResponseEntity.getStatusCode(), is(HttpStatus.OK));
            assertThat(user.getLocationsBlacklist(), is(userBlacklistResponseEntity.getBody()));
        });
    }

    @Test
    void shouldThrowExceptionWhenProcessingLocationBlacklistOfInvalidUser() {
        //given
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        Map<String, String> deleteFromBlacklistData = new HashMap<>();
        deleteFromBlacklistData.put("userId", "1");
        deleteFromBlacklistData.put("locationId", "1");

        //when
        //then
        assertAll(() -> {
            ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class,
                    () -> userService
                            .getUserBlacklist(2L));
            assertEquals("User not found", resourceNotFoundException.getMessage());
            resourceNotFoundException = assertThrows(ResourceNotFoundException.class,
                    () -> userService
                            .deleteFromUserBlacklist(deleteFromBlacklistData));
            assertEquals("User not found", resourceNotFoundException.getMessage());
        });
    }

    @Test
    void shouldThrowExceptionWhenDeletingInvalidLocationFromUserBlacklist() {
        //given
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(locationRepository.findById(any())).thenReturn(Optional.empty());

        Map<String, String> deleteFromBlacklistData = new HashMap<>();
        deleteFromBlacklistData.put("userId", "1");
        deleteFromBlacklistData.put("locationId", "1");

        //when
        //then
        assertAll(() -> {
            ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class,
                    () -> userService
                            .deleteFromUserBlacklist(deleteFromBlacklistData));
            assertEquals("Location not found", resourceNotFoundException.getMessage());
        });
    }

    @Test
    void shouldDeleteLocationFromUserBlacklist() {
        //given
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(locationRepository.findById(any())).thenReturn(Optional.of(locations.get(0)));
        int userBlacklistSize = user.getLocationsBlacklist().size();
        Map<String, String> deleteFromBlacklistData = new HashMap<>();
        deleteFromBlacklistData.put("userId", "2");
        deleteFromBlacklistData.put("locationId", "1");

        //when
        userService.deleteFromUserBlacklist(deleteFromBlacklistData);

        //then
        verify(userRepository, times(1)).save(userCaptor.capture());
        assertAll(() -> {
            User user1 = userCaptor.getAllValues().get(0);
            assertThat(user1.getLocationsBlacklist().size(), is(userBlacklistSize - 1));
            assertThat(user1.getLocationsBlacklist(), containsInAnyOrder(locations.get(2), locations.get(4)));
            assertThat(user1.getLocationsBlacklist(), not(contains(locations.get(0))));
        });

    }
}