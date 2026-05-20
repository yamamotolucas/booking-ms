package br.com.booking_ms;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Debug {
    public static void main(String[] args) {


        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);

        if(!startTime.isBefore(endTime))
        {
            System.out.println("oi");
        }
    }
}
