package com.pleczycki.tgtg.service;

import com.pleczycki.tgtg.exception.ResourceNotFoundException;
import com.pleczycki.tgtg.dto.ReviewDto;
import com.pleczycki.tgtg.model.Address;
import com.pleczycki.tgtg.model.Location;
import com.pleczycki.tgtg.model.Review;
import com.pleczycki.tgtg.model.User;
import com.pleczycki.tgtg.repository.LocationRepository;
import com.pleczycki.tgtg.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.time.DateUtils.addDays;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private LocationRepository locationRepository;

    @Captor
    private ArgumentCaptor<Review> reviewCaptor;

    private static List<Review> allReviews;
    private static ReviewDto reviewDto;
    private static Review reviewWithExistingLocation;
    private static Review reviewWithNewLocation;
    private static User user;
    private static List<Location> existingLocations = new LinkedList<>();
    private static Location newLocation;

    @BeforeEach
    void setup() throws ParseException {
        MockitoAnnotations.initMocks(this);

        user = initializeUser();
        existingLocations = getExistingLocations();
        newLocation = initializeNewLocation();
        reviewWithExistingLocation = initializeReviewWithExistingLocation();
        reviewWithNewLocation = initializeReviewWithNewLocation();
        allReviews = getReviewList();
        reviewDto = initializeReviewDto();
    }

    private static List<Review> getReviewList() throws ParseException {
        List<Review> reviews = new ArrayList<>();
        String dateInString = "10-01-2020";
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        Date date = formatter.parse(dateInString);
        for (int i = 1; i <= 10; i++) {
            final Review review = new Review();
            review.setId((long) (i));
            review.setCreatedAt(addDays(date, i));
            review.setModifiedAt(addDays(date, i));
            review.setComment("comment" + i);
            review.setRating((i % 4) + 1);
            review.setStandardPrice(i * 6);
            review.setDiscountPrice(i * 2);
            review.setPickupTime(addDays(date, i * 20));

            reviews.add(review);
        }
        return reviews;
    }

    private static Review initializeReviewWithExistingLocation() {
        final Review review = new Review();

        review.setId(1L);
        review.setLocation(existingLocations.get(0));
        review.setCreatedAt(new Date());
        review.setComment("comment");
        review.setRating(5);
        review.setStandardPrice(30);
        review.setDiscountPrice(10);
        review.setPickupTime(new Date());

        return review;
    }

    private static Review initializeReviewWithNewLocation() {
        final Review review = new Review();

        review.setId(2L);
        review.setLocation(newLocation);
        review.setCreatedAt(new Date());
        review.setComment("comment");
        review.setRating(5);
        review.setStandardPrice(30);
        review.setDiscountPrice(10);
        review.setPickupTime(new Date());

        return review;
    }

    private ReviewDto convertReviewToDto(Review review) {
        final ReviewDto reviewDto = new ReviewDto();

        reviewDto.setLocation(review.getLocation());
        reviewDto.setComment(review.getComment());
        reviewDto.setRating(review.getRating());
        reviewDto.setStandardPrice(review.getStandardPrice());
        reviewDto.setDiscountPrice(review.getDiscountPrice());
        reviewDto.setPickupTime(review.getPickupTime());

        return reviewDto;
    }

    private ReviewDto initializeReviewDto() {
        final ReviewDto reviewDto = new ReviewDto();

        reviewDto.setLocation(existingLocations.get(1));
        reviewDto.setComment("dto comment");
        reviewDto.setRating(5);
        reviewDto.setStandardPrice(30);
        reviewDto.setDiscountPrice(10);
        reviewDto.setPickupTime(new Date());

        return reviewDto;
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

    private static Location initializeNewLocation() {
        final Location location = new Location();
        final Address address = new Address();

        address.setId(1L);
        address.setStreet("Floriańska");
        address.setBuildingNo("21a");
        address.setCity("Siedlce");
        address.setLatitude(52.165657);
        address.setLongitude(22.283413);

        location.setAddress(address);
        location.setName("Dom");
        location.setRating(5.0);

        return location;
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

        return user;
    }

    @Test
    void shouldAddReviewWithExistingLocation() {
        //given
        when(locationRepository.findById(any())).thenReturn(Optional.of(reviewDto.getLocation()));

        //when
        Review review = reviewService.addReview(reviewDto);

        //then

        verify(reviewRepository, times(1)).save(reviewCaptor.capture());
        assertAll(() -> {
            Review review1 = reviewCaptor.getAllValues().get(0);
            verify(locationRepository, times(1)).findById(any());
            assertNotNull(review1);
            assertThat(review1.getComment(), is(reviewDto.getComment()));
            assertThat(review1.getLocation(), is(sameInstance(reviewDto.getLocation())));
            assertThat(review1.getLocation().getId(), is(reviewDto.getLocation().getId()));
        });
    }

    @Test
    void shouldAddReviewWithNewLocation() {
        //given
        reviewDto.setLocation(newLocation);
        //when
        Review review = reviewService.addReview(reviewDto);

        //then
        verify(reviewRepository, times(1)).save(reviewCaptor.capture());
        assertAll(() -> {
            Review review1 = reviewCaptor.getAllValues().get(0);
            verify(locationRepository, times(0)).findById(any());
            assertNotNull(review1);
            assertThat(review1.getComment(), is(reviewDto.getComment()));
            assertThat(review1.getLocation(), is(sameInstance(newLocation)));
        });
    }

    @Test
    void shouldFindReviewById() {
        //given
        when(reviewRepository.findById(any())).thenReturn(Optional.of(reviewWithExistingLocation));

        //when
        ResponseEntity<Review> reviewResponseEntity = reviewService.getReview(1L);

        //then
        assertThat(reviewResponseEntity.getBody(), is(reviewWithExistingLocation));
    }

    @Test
    void shouldUpdateReviewWithExistingLocation() {

        //given
        when(reviewRepository.findById(any())).thenReturn(Optional.of(new Review(reviewWithExistingLocation)));
        when(locationRepository.findById(any())).thenReturn(Optional.of(existingLocations.get(1)));

        //when
        Review review = reviewService
                .updateReview(reviewDto, reviewWithExistingLocation.getId());

        //then
        verify(reviewRepository, times(1)).save(reviewCaptor.capture());
        assertAll(() -> {
            Review review1 = reviewCaptor.getAllValues().get(0);
            assertThat(review1.getId(), is(reviewWithExistingLocation.getId()));
            assertThat(review1.getLocation(), not(sameInstance(reviewWithExistingLocation.getLocation())));
            assertThat(review1.getLocation().getName(), not(reviewWithExistingLocation.getLocation().getName()));
            assertThat(review1.getLocation(), sameInstance(reviewDto.getLocation()));
            assertThat(review1.getLocation().getRating(), is(reviewDto.getLocation().getRating()));
            assertThat(review1.getComment(), is(not(reviewWithExistingLocation.getComment())));
            assertThat(review1.getComment(), is(reviewDto.getComment()));
        });
    }

    @Test
    void shouldUpdateReviewWithNewLocation() {

        //given
        when(reviewRepository.findById(any())).thenReturn(Optional.of(new Review(reviewWithExistingLocation)));
        reviewDto.setLocation(newLocation);

        //when
        Review review = reviewService
                .updateReview(reviewDto, reviewWithExistingLocation.getId());

        //then
        verify(reviewRepository, times(1)).save(reviewCaptor.capture());
        assertAll(() -> {
            Review review1 = reviewCaptor.getAllValues().get(0);
            assertThat(review1.getId(), is(reviewWithExistingLocation.getId()));
            assertThat(review1.getLocation().getId(), not(reviewWithExistingLocation.getLocation().getId()));
            assertThat(review1.getLocation(), not(sameInstance(reviewWithExistingLocation.getLocation())));
            assertThat(review1.getLocation(), sameInstance(reviewDto.getLocation()));
            assertThat(review1.getLocation().getRating(), is(reviewDto.getLocation().getRating()));
            assertThat(review1.getComment(), is(not(reviewWithExistingLocation.getComment())));
            assertThat(review1.getComment(), is(reviewDto.getComment()));
        });
    }

    @Test
    void shouldThrowValidExceptionsWhenProcessingReviewsWithNonExistentLocation() {
        //given
        when(reviewRepository.findById(any())).thenReturn(Optional.of(reviewWithExistingLocation));
        when(locationRepository.findById(any())).thenReturn(Optional.empty());

        //when
        //then
        assertAll(() -> {
            ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class,
                    () -> reviewService
                            .updateReview(reviewDto, 1L));
            assertEquals("Location not found", resourceNotFoundException.getMessage());
            resourceNotFoundException = assertThrows(ResourceNotFoundException.class,
                    () -> reviewService
                            .addReview(reviewDto));
            assertEquals("Location not found", resourceNotFoundException.getMessage());
        });
    }

    @Test
    void shouldThrowValidExceptionsWhenProcessingReviewsWithNotExistingId() {
        //given
        when(reviewRepository.findById(any())).thenReturn(Optional.empty());

        //when
        //then
        assertAll(() -> {
            ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class,
                    () -> reviewService.getReview(1L));
            assertEquals("Review not found", resourceNotFoundException.getMessage());
            resourceNotFoundException = assertThrows(ResourceNotFoundException.class,
                    () -> reviewService
                            .updateReview(reviewDto, 1L, new ArrayList<>(), new ArrayList<>()));
            assertEquals("Review not found", resourceNotFoundException.getMessage());
        });
    }

    @Test
    void reviewShouldBeDeleted() {

        //given
        when(reviewRepository.getOne(any())).thenReturn(reviewWithExistingLocation);

        //when
        reviewService.delete(1L);
        verify(reviewRepository, times(1)).deleteById(anyLong());

        assertThrows(ResourceNotFoundException.class, () -> {
            reviewService.getReview(1L);
        });
    }

    @Test
    void shouldReturnAllReviews() {
        //given
        when(reviewRepository.findAll()).thenReturn(allReviews);

        //when
        ResponseEntity<List<Review>> responseEntity = reviewService.getAllReviews();

        //then
        assertAll(() -> {
            assertNotNull((responseEntity.getBody()));
            assertThat(responseEntity.getBody().size(), is(allReviews.size()));
            assertThat(responseEntity.getBody().get(0), is(allReviews.get(0)));
        });
    }
}