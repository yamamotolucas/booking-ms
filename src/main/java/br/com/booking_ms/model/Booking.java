package br.com.booking_ms.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "barber_id", nullable = false)
    private Long barberId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Column(name = "booking_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus;

    @Column(name = "confirmation_token", nullable = false, unique = true)
    private String confirmationToken;

    @Column(name = "barber_name")
    private String barberName;


    @Column(name = "customer_name")
    private String customerName;

    @ManyToMany
    @JoinTable(
            name = "booking_services",
            joinColumns = @JoinColumn(name = "booking_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private List<BarberServices> services = new ArrayList<>();

    public Booking() {}

    public Long getBookingId() {return bookingId;}

    public void setBookingId(Long bookingId) {this.bookingId = bookingId;}

    public Long getCustomerId() {return customerId;}

    public void setCustomerId(Long customerId) {this.customerId = customerId;}

    public Long getBarberId() {return barberId;}

    public void setBarberId(Long barberId) {this.barberId = barberId;}

    public LocalDateTime getStartTime() {return startTime;}

    public void setStartTime(LocalDateTime startTime) {this.startTime = startTime;}

    public LocalDateTime getEndTime() {return endTime;}

    public void setEndTime(LocalDateTime endTime) {this.endTime = endTime;}

    public List<BarberServices> getServices() {return services;}

    public void setServices(List<BarberServices> services) {this.services = services;}

    public BookingStatus getBookingStatus() {return bookingStatus;}

    public void setBookingStatus(BookingStatus bookingStatus) {this.bookingStatus = bookingStatus;}

    public String getConfirmationToken() {return confirmationToken;}

    public void setConfirmationToken(String confirmationToken) {this.confirmationToken = confirmationToken;}

    public String getBarberName() {return barberName;}

    public void setBarberName(String barberName) {this.barberName = barberName;}

    public String getCustomerName() {return customerName;}

    public void setCustomerName(String customerName) {this.customerName = customerName;}

}
