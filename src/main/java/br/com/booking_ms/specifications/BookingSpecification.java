package br.com.booking_ms.specifications;

import br.com.booking_ms.model.Booking;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;
import java.util.List;

public class BookingSpecification {

    public static Specification<Booking> hasCustomerId(Long customerId) {
        return (root, query, cb) -> cb.equal(root.get("customerId"), customerId);
    }

    public static Specification<Booking> hasBarberId(Long barberId) {
        return (root, query, cb) -> cb.equal(root.get("barberId"), barberId);
    }

    public static Specification<Booking> hasStatusIn(List<String> statuses) {
        return (root, query, cb) -> root.get("bookingStatus").in(statuses);
    }

    public static Specification<Booking> startTimeGreaterThanOrEqualTo(LocalDateTime start) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startTime"), start);
    }

    public static Specification<Booking> startTimeLessThanOrEqualTo(LocalDateTime end) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("startTime"), end);
    }

    public static Specification<Booking> isPastOrInactive(LocalDateTime now) {
        return (root, query, cb) -> {
            Predicate isPast = cb.lessThan(root.get("startTime"), now);
            Predicate isInactiveStatus = root.get("bookingStatus").in("REJECTED", "CANCELLED");
            return cb.or(isPast, isInactiveStatus);
        };
    }
}
