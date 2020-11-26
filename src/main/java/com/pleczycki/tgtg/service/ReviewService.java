package com.pleczycki.tgtg.service;

import com.pleczycki.tgtg.dto.ReviewDto;
import com.pleczycki.tgtg.model.Location;
import com.pleczycki.tgtg.model.Review;
import com.pleczycki.tgtg.model.User;
import com.pleczycki.tgtg.repository.LocationRepository;
import com.pleczycki.tgtg.repository.ReviewRepository;
import com.pleczycki.tgtg.repository.UserRepository;
import com.pleczycki.tgtg.utils.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
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
    private LocationRepository locationRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PhotoService storageService;

    @Transactional
    public ApiResponse addReview(ReviewDto reviewDto, Long userId, List<MultipartFile> files) {
        Review review = new Review();
        review.setPickupTime(reviewDto.getPickupTime());
        review.setDiscountPrice(reviewDto.getDiscountPrice());
        review.setStandardPrice(reviewDto.getStandardPrice());
        review.setRating(reviewDto.getRating());
        review.setComment(reviewDto.getComment());

        if(Objects.nonNull(reviewDto.getLocation().getId())){
            Location location = locationRepository.getOne(reviewDto.getLocation().getId());
            review.setLocation(location);
        } else {
            review.setLocation(reviewDto.getLocation());
        }

        review.getLocation().setCreatedAt(new Date());
        review.setCreatedAt(new Date());

        User user = userRepository.getOne(userId);
        Review savedReview = reviewRepository.save(review);

        Location location = locationRepository.getOne(savedReview.getLocation().getId());
        location.setRating(locationRepository.getAverageRating(location.getId()));
        locationRepository.save(location);

        storageService.store(files, savedReview);

        List<Review> userReviews = user.getReviews();
        userReviews.add(savedReview);
        user.setReviews(userReviews);
        userRepository.save(user);
        return new ApiResponse(true, "" + savedReview.getId());
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    public List<Review> getAllReviewsByUserId(Long userId) {
        return reviewRepository.findAllByUserId(userId);
    }

    public List<Review> getLatestReviews(int limit) {
        return reviewRepository.getLatestReviews(limit);
    }

    public Review getReview(Long id) {
        Optional<Review> review = reviewRepository.findById(id);
        return review.orElse(null);
    }

    public List<Review> getLatestLocationReviews(long locationId) {
        return reviewRepository.getLatestLocationReviews(locationId);
    }

    public ApiResponse updateReview(ReviewDto reviewDto, long reviewId, List<MultipartFile> files,
            List<String> deletedPhotosIds) {

        Review review = reviewRepository.getOne(reviewId);
        review.setPickupTime(reviewDto.getPickupTime());
        review.setDiscountPrice(reviewDto.getDiscountPrice());
        review.setStandardPrice(reviewDto.getStandardPrice());
        review.setRating(reviewDto.getRating());
        review.setComment(reviewDto.getComment());

        if(Objects.nonNull(reviewDto.getLocation().getId())){
            Location location = locationRepository.getOne(reviewDto.getLocation().getId());
            review.setLocation(location);
        } else {
            review.setLocation(reviewDto.getLocation());
        }

        review.getLocation().setCreatedAt(new Date());
        review.setCreatedAt(new Date());
        Review savedReview = reviewRepository.save(review);

        Location location = locationRepository.getOne(savedReview.getLocation().getId());
        location.setRating(locationRepository.getAverageRating(location.getId()));
        locationRepository.save(location);

        storageService.store(files, savedReview);
        deletedPhotosIds.forEach(id -> storageService.deleteById(id));

        return new ApiResponse(true, "" + savedReview.getId());
    }

    public void delete(Long id) {
        reviewRepository.deleteById(id);
    }
}
