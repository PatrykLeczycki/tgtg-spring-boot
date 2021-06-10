package com.pleczycki.tgtg.service;

import com.pleczycki.tgtg.exception.ResourceNotFoundException;
import com.pleczycki.tgtg.dto.ReviewDto;
import com.pleczycki.tgtg.model.Location;
import com.pleczycki.tgtg.model.Review;
import com.pleczycki.tgtg.repository.LocationRepository;
import com.pleczycki.tgtg.repository.ReviewRepository;
import com.pleczycki.tgtg.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.transaction.Transactional;

@Service("ReviewService")
public class ReviewService {

    @Autowired
    private LocationService locationService;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PhotoService photoService;

    @Transactional
    public ResponseEntity<Review> addReview(ReviewDto reviewDto, Long userId, List<MultipartFile> files) {
        Review review = addReview(reviewDto);
        userService.addReview(userId, review);
        photoService.store(files, review);
        locationService.updateRating(review.getLocation().getId());
        return ResponseEntity.ok(review);
    }

    @Transactional
    public Review addReview(ReviewDto reviewDto) {
        Review review = new Review();
        review.setPickupTime(reviewDto.getPickupTime());
        review.setDiscountPrice(reviewDto.getDiscountPrice());
        review.setStandardPrice(reviewDto.getStandardPrice());
        review.setRating(reviewDto.getRating());
        review.setComment(reviewDto.getComment());

        if (Objects.nonNull(reviewDto.getLocation().getId())) {
            Optional<Location> optionalLocation = locationRepository.findById(reviewDto.getLocation().getId());

            if (optionalLocation.isEmpty()) {
                throw new ResourceNotFoundException("Location not found");
            }

            review.setLocation(optionalLocation.get());
        } else {
            Optional<Location> existingLocation = locationRepository.findAll().stream().
                    filter(loc -> loc.equals(reviewDto.getLocation())).
                    findFirst();

            if (existingLocation.isPresent()) {
                review.setLocation(existingLocation.get());
            } else {
                Objects.requireNonNull(reviewDto.getLocation()).setCreatedAt(new Date());
                review.setLocation(reviewDto.getLocation());
            }
        }

        review.setCreatedAt(new Date());
        return reviewRepository.save(review);
    }

    @Transactional
    public ResponseEntity<Review> updateReview(ReviewDto reviewDto, Long userId, List<MultipartFile> files,
                                               List<String> deletedPhotosIds) {
        Review review = updateReview(reviewDto, userId);
        locationService.updateRating(review.getLocation().getId());
        photoService.store(files, review);
        photoService.deletePhotos(deletedPhotosIds);
        return ResponseEntity.ok(review);
    }

    @Transactional
    public Review updateReview(ReviewDto reviewDto, long reviewId) {

        Optional<Review> optionalReview = reviewRepository.findById(reviewId);

        if (optionalReview.isEmpty()) {
            throw new ResourceNotFoundException("Review not found");
        }

        Review review = optionalReview.get();

        review.setPickupTime(reviewDto.getPickupTime());
        review.setDiscountPrice(reviewDto.getDiscountPrice());
        review.setStandardPrice(reviewDto.getStandardPrice());
        review.setRating(reviewDto.getRating());
        review.setComment(reviewDto.getComment());

        if (Objects.nonNull(reviewDto.getLocation().getId())) {
            Optional<Location> optionalLocation = locationRepository.findById(reviewDto.getLocation().getId());

            if (optionalLocation.isEmpty()) {
                throw new ResourceNotFoundException("Location not found");
            }

            review.setLocation(optionalLocation.get());
        } else {
            reviewDto.getLocation().setCreatedAt(new Date());
            review.setLocation(reviewDto.getLocation());
        }

        review.setModifiedAt(new Date());

        return reviewRepository.save(review);
    }

    public ResponseEntity<List<Review>> getAllReviews() {
        List<Review> reviews = reviewRepository.findAll();
        return ResponseEntity.ok(reviews);
    }

    public ResponseEntity<List<Review>> getAllReviewsByUserId(Long userId) {
        List<Review> reviews = reviewRepository.findAllByUserId(userId);
        return ResponseEntity.ok(reviews);
    }

    public ResponseEntity<List<Review>> getLatestReviews(int limit) {
        List<Review> reviews = reviewRepository.getLatestReviews(limit);
        return ResponseEntity.ok(reviews);
    }

    public ResponseEntity<Review> getReview(Long id) {
        Optional<Review> review = reviewRepository.findById(id);

        if (review.isEmpty()) {
            throw new ResourceNotFoundException("Review not found");
        }

        return ResponseEntity.of(review);
    }

    public ResponseEntity<List<Review>> getLatestLocationReviews(long locationId) {
        List<Review> latestLocationReviews = reviewRepository.getLatestLocationReviews(locationId);
        return ResponseEntity.ok(latestLocationReviews);
    }

    @Modifying
    @Transactional
    public ResponseEntity<ApiResponse> delete(Long reviewId, String userId) {

        long dbUserId = reviewRepository.getUserId(reviewId);

        if (dbUserId == Long.parseLong(userId)) {

            Review review = reviewRepository.getOne(reviewId);
            Location location = review.getLocation();

            reviewRepository.deleteUserReview(reviewId);
            reviewRepository.deleteById(reviewId);

            if (reviewRepository.countAllByLocationId(location.getId()) == 0) {
                location.setRating(0.0);
            } else {
                location.setRating(locationRepository.getAverageRating(location.getId()));
            }

            locationRepository.save(location);

            return ResponseEntity.ok(new ApiResponse(true, "Review deleted successfully"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, "Deleting review disallowed"));
    }
}
