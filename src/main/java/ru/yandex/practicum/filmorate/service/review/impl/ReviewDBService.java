package ru.yandex.practicum.filmorate.service.review.impl;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.review.ReviewDAO;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.review.ReviewService;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service("ReviewDBService")
@Primary
@AllArgsConstructor
public class ReviewDBService implements ReviewService {
    
    private final ReviewDAO reviewDAO;

    @Override
    public Review getReviewById(int reviewId) {
        return reviewDAO.getReviewById(reviewId);
    }

    @Override
    public Collection<Review> getAllReviews() {
        return reviewDAO.getAllReviews().stream()
                .sorted(Comparator.comparing(Review::getUseful).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Review> getFilmReviewsSortedByUsefulness(int filmId, int count) {
        if (count == 0) {
            count = Integer.MAX_VALUE;
        }

        return reviewDAO.getReviewsByFilmId(filmId).stream()
                .sorted(Comparator.comparing(Review::getUseful).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    @Override
    public Review addReview(Review review) {
        return reviewDAO.addReview(review);
    }

    @Override
    public void addValueToReview(int reviewId, int userId, boolean isLike) {
        reviewDAO.addValueToReview(reviewId, userId, isLike);
    }

    @Override
    public Review updateReview(Review review) {
        return reviewDAO.updateReview(review);
    }

    @Override
    public void removeReview(int reviewId) {
        reviewDAO.removeReview(reviewId);
    }

    @Override
    public void removeValueFromReview(int reviewId, int userId, boolean isLike) {
        reviewDAO.removeValueFromReview(reviewId, userId, isLike);
    }
}
