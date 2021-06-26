package com.pleczycki.tgtg.controller;

import com.pleczycki.tgtg.dto.ReviewDto;
import com.pleczycki.tgtg.model.Review;
import com.pleczycki.tgtg.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/review")
@Slf4j
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/add")
    public ResponseEntity<Review> addReview(@RequestPart("review") ReviewDto reviewDto, @RequestPart("id") Long userId,
                                 @RequestPart("files") List<MultipartFile> files) {
        return reviewService.addReview(reviewDto, userId, files);
    }

    @PostMapping("/edit/{id}")
    public ResponseEntity<Review> updateReview(@RequestPart("review") ReviewDto reviewDto,
                                    @RequestPart("files") List<MultipartFile> files, @RequestPart("deleted") List<String> deletedPhotosIds,
                                    @PathVariable("id") Long reviewId) {
        return reviewService.updateReview(reviewDto, reviewId, files, deletedPhotosIds);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Review>> getAllReviews() {
        return reviewService.getAllReviews();
    }

    @GetMapping("/all/user/{id}")
    public ResponseEntity<List<Review>> getAllReviewsByUserId(@PathVariable("id") Long id) {
        return reviewService.getAllReviewsByUserId(id);
    }

    @GetMapping("/latest")
    public ResponseEntity<List<Review>> getLatestReviews(@RequestParam int limit) {
        return reviewService.getLatestReviews(limit);
    }

    @GetMapping("/location/{id}/latest")
    public ResponseEntity<List<Review>> getLatestLocationReviews(@PathVariable("id") long locationId) {
        return reviewService.getLatestLocationReviews(locationId);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<Review> getReview(@PathVariable Long id) {
        return reviewService.getReview(id);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable("id") Long reviewId, @RequestBody String userId) {
        reviewService.delete(reviewId, userId);
    }
}
