package com.mteam.sleerenthome.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

import com.mteam.sleerenthome.model.Room;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookedRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @Column(name = "check_in")
    private LocalDate checkInDate;

    @Column(name = "check_out")
    private LocalDate checkOutDate;

    @Column(name = "guest_fullName")
    private String guestFullName;

    @Column(name = "guest_email")
    private String guestEmailAddress;

    @Column(name = "adults")
    private int NumOfAdults;

    @Column(name = "cheldren")
    private int NumOfCheldren;

    @Column(name = "total_guest")
    private int totalNumOfGuest;

    @Column(name = "confirmation_code")
    private String bookingConfirmationCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    public void calulateTotalNumberOfGuest() {
        this.totalNumOfGuest = this.NumOfAdults + this.NumOfCheldren;
    }

    public void setNumOfAdults(int numOfAdults) {
        NumOfAdults = numOfAdults;
        calulateTotalNumberOfGuest();
    }

    public void setNumOfCheldren(int numOfCheldren) {
        NumOfCheldren = numOfCheldren;
        calulateTotalNumberOfGuest();
    }

    public void setBookingConfirmationCode(String bookingConfirmationCode) {
        this.bookingConfirmationCode = bookingConfirmationCode;
    }
}
