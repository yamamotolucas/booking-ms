package br.com.booking_ms.repository;

import br.com.booking_ms.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT (COUNT(b) > 0) FROM Booking b WHERE CAST(b.startTime AS date) = :date AND b.customerId = :customerId")
    boolean existsByDateAndCustomerId(@Param("date") LocalDate date, @Param("customerId") Long customerId);
}
