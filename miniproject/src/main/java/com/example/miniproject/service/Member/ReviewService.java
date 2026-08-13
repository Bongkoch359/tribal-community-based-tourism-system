package com.example.miniproject.service.Member;

import com.example.miniproject.entity.Booking;
import com.example.miniproject.entity.Review;
import com.example.miniproject.entity.enums.BookingStatus;
import com.example.miniproject.repository.Member.BookingRepository;
import com.example.miniproject.repository.Member.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookingRepository bookingRepository;

    // ══════════════════════════════════════════════════════
    //  Generate Review ID  →  RV001, RV002, ...
    // ══════════════════════════════════════════════════════
    private String generateReviewId() {
        long count = reviewRepository.countAll() + 1;
        return String.format("RV%03d", count);
    }

    // ══════════════════════════════════════════════════════
    //  Submit Review (เดิม — สำหรับ member)
    // ══════════════════════════════════════════════════════
    public void submitReview(String bookingId,
                         String memberId,
                         Integer rating,
                         String comment,
                         MultipartFile[] imageFiles,
                          boolean anonymous) throws IOException {

    Booking booking = bookingRepository.findById(bookingId)
        .orElseThrow(() -> new IllegalArgumentException("ไม่พบการจองนี้"));

    if (!booking.getMember().getMemberid().equals(memberId))
        throw new IllegalStateException("ไม่มีสิทธิ์รีวิวการจองนี้");

    if (booking.getBookingStatus() != BookingStatus.COMPLETED)
        throw new IllegalStateException("สามารถรีวิวได้เฉพาะการจองที่เข้าพักเสร็จสิ้นแล้ว");

    if (reviewRepository.findByBookingBookingid(bookingId).isPresent())
        throw new IllegalStateException("คุณได้รีวิวการจองนี้ไปแล้ว");

    if (rating == null || rating < 1 || rating > 5)
        throw new IllegalArgumentException("กรุณาให้คะแนน 1-5 ดาว");

    List<String> savedPaths = new ArrayList<>();
    if (imageFiles != null) {
        String uploadDir = "uploads/reviews/";
        Files.createDirectories(Paths.get(uploadDir));
        for (MultipartFile file : imageFiles) {
            if (file != null && !file.isEmpty()) {
                String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path savePath = Paths.get(uploadDir + filename);
                Files.copy(file.getInputStream(), savePath, StandardCopyOption.REPLACE_EXISTING);
                savedPaths.add("/" + uploadDir + filename);
            }
        }
    }
    String joinedPaths = String.join(",", savedPaths);

    Review review = new Review();
    review.setReviewid(generateReviewId());
    review.setBooking(booking);
    review.setRating(rating);
    review.setComment(comment != null ? comment.trim() : "");
    review.setReviewimages(joinedPaths.isEmpty() ? null : joinedPaths);
    review.setReviewdate(Date.valueOf(LocalDate.now()));
    review.setAnonymous(anonymous);
    reviewRepository.save(review);
}

    // ══════════════════════════════════════════════════════
    //  ดึงรีวิวของทัวร์ (สำหรับ manager)
    // ══════════════════════════════════════════════════════
    public List<Review> getReviewsByTourId(String tourid) {
        return reviewRepository.findByTourId(tourid);
    }

    // ══════════════════════════════════════════════════════
    //  ค่าเฉลี่ย rating ของทัวร์
    // ══════════════════════════════════════════════════════
    public double getAvgRatingByTourId(String tourid) {
        Double avg = reviewRepository.avgRatingByTourId(tourid);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    // ══════════════════════════════════════════════════════
    //  นับจำนวนรีวิวแต่ละดาว  →  Map<Integer(ดาว), Long(จำนวน)>
    // ══════════════════════════════════════════════════════
    public Map<Integer, Long> getRatingCountsByTourId(String tourid) {
        List<Review> reviews = reviewRepository.findByTourId(tourid);
        return reviews.stream()
                .collect(Collectors.groupingBy(Review::getRating, Collectors.counting()));
    }


public List<Review> getReviewsByHomestayId(Integer homestayId) {
    return reviewRepository.findByHomestayId(homestayId);
}

public double getAvgRatingByHomestayId(Integer homestayId) {
    Double avg = reviewRepository.avgRatingByHomestayId(homestayId);
    return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
}

public long getReviewCountByHomestayId(Integer homestayId) {
    Long count = reviewRepository.countByHomestayId(homestayId);
    return count != null ? count : 0L;
}
}