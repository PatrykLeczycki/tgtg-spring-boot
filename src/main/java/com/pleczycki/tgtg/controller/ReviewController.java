package com.pleczycki.tgtg.controller;

import com.pleczycki.tgtg.dto.ReviewDto;
import com.pleczycki.tgtg.model.Review;
import com.pleczycki.tgtg.service.ReviewService;
import com.pleczycki.tgtg.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/add")
    public ApiResponse addReview(@RequestPart("review") ReviewDto reviewDto, @RequestPart("id") Long userId,
                                 @RequestPart("files") List<MultipartFile> files) {
        return reviewService.addReview(reviewDto, userId, files);
    }

    @PostMapping("/edit/{id}")
    public ApiResponse updateReview(@RequestPart("review") ReviewDto reviewDto,
                                    @RequestPart("files") List<MultipartFile> files, @RequestPart("deleted") List<String> deletedPhotosIds,
                                    @PathVariable("id") Long reviewId) {
        return reviewService.updateReview(reviewDto, reviewId, files, deletedPhotosIds);
    }

    @GetMapping("/all")
    public List<Review> getAllReviews() {
        return reviewService.getAllReviews();
    }

    @GetMapping("/all/user/{id}")
    public List<Review> getAllReviewsByUserId(@PathVariable("id") Long id) {
        return reviewService.getAllReviewsByUserId(id);
    }

    @GetMapping("/latest")
    public List<Review> getLatestReviews(@RequestParam int limit) {
        return reviewService.getLatestReviews(limit);
    }

    @GetMapping("/location/{id}/latest")
    public List<Review> getLatestLocationReviews(@PathVariable("id") long locationId) {
        return reviewService.getLatestLocationReviews(locationId);
    }

    @GetMapping("/get/{id}")
    public Review getReview(@PathVariable Long id) {
        return reviewService.getReview(id);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable("id") Long id) {
        reviewService.delete(id);
    }
}
