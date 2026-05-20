package br.com.booking_ms.repository;

import br.com.booking_ms.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT (COUNT(b) > 0) FROM Booking b WHERE CAST(b.startTime AS date) = :date AND b.customerId = :customerId")
    boolean existsByDateAndCustomerId(@Param("date") LocalDate date, @Param("customerId") Long customerId);

    @Query("SELECT (COUNT(b) > 0) FROM Booking b WHERE b.startTime < :endTime AND b.endTime > :startTime")
    boolean existsByDateAndStartTime(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT b FROM Booking b WHERE b.barberId = :barberId AND CAST(b.startTime AS date) = :date")
    List<Booking> findByBarberIdAndDate(@Param("barberId") Long barberId, @Param("date") LocalDate date);

    List<Booking> findByCustomerId(Long customerId);

    Optional<Booking> findByBookingIdAndCustomerId(Long bookingId, Long customerId);
}
