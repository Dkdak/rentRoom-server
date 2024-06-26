package com.mteam.sleerenthome.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;

@Entity
@Getter
@Setter
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomType;
    private BigDecimal roomPrice;
    private boolean isBooked = false;

    @Lob
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // JSON 직렬화에서 무시할 필드
    private Blob photo;


    @Transient
    @JsonProperty("photo") // JSON 필드 이름 지정
    private String photoBase64;

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BookedRoom> bookings;

    public Room() {
        this.bookings = new ArrayList<>();
    }

    public void addBooking(BookedRoom booking) {
        if(bookings == null) {
            bookings = new ArrayList<>();
        }

        bookings.add(booking);
        booking.setRoom(this);
        isBooked = true;

        String bookingCode = RandomStringUtils.randomNumeric(10);
        booking.setBookingConfirmationCode(bookingCode);

    }
}
