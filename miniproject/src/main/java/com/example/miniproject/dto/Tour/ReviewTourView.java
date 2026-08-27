package com.example.miniproject.dto.Tour;

import com.example.miniproject.entity.Review;
import com.example.miniproject.entity.Tour;

public class ReviewTourView {
    private final Review review;
    private final Tour tour;

    public ReviewTourView(Review review, Tour tour) {
        this.review = review;
        this.tour = tour;
    }

    public Review getReview() {
        return review;
    }

    public Tour getTour() {
        return tour;
    }
}
