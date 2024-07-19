package com.mteam.sleerenthome.service;

import com.mteam.sleerenthome.exception.InvalidBookingRequestException;
import com.mteam.sleerenthome.exception.ResourceNotFoundException;
import com.mteam.sleerenthome.model.BookedRoom;
import com.mteam.sleerenthome.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingService implements IBookingService{

    private final BookingRepository bookedRoomRepository;
    private final IRoomService roomService;
    private final BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public List<BookedRoom> getAllBookingsByRoomId(Long roomId) {
        return bookedRoomRepository.findAllByRoomId(roomId);
    }


    @Override
    public List<BookedRoom> getAllBookings() {
        return bookedRoomRepository.findAll();
    }


    @Override
    public void cancelBooking(Long bookingId) {
        bookedRoomRepository.deleteById(bookingId);
    }

    @Override
    public String saveBooking(Long roomId, BookedRoom bookingRequest) {
        if(bookingRequest.getCheckOutDate().isBefore(bookingRequest.getCheckInDate())) {
            throw new InvalidBookingRequestException("check-in date must come before check-out date");
        }

        roomService.getRoomById(roomId).ifPresentOrElse(
                room -> {
                    // do something with room
                    List<BookedRoom> existingBookings = room.getBookings();
                    boolean roomIsAvailable = roomIsAvailable(bookingRequest, existingBookings);
                    if(roomIsAvailable) {
                        room.addBooking(bookingRequest);
                        bookingRepository.save(bookingRequest);
                    } else {
                        throw new InvalidBookingRequestException("Sorry, this room is not available for the selected dates");
                    }
                },
                () -> {
                    // handle the case where room is not found
                    throw new ResourceNotFoundException("Room not found");
                }
        );

        return bookingRequest.getBookingConfirmationCode();
    }



    @Override
    public BookedRoom findByBookingConfirmationCode(String confirmationCode) {
        return bookingRepository.findByBookingConfirmationCode(confirmationCode)
                .orElseThrow(()->new ResourceNotFoundException("No Booking found with booking code: " + confirmationCode));
    }


    private boolean roomIsAvailable(BookedRoom bookingRequest, List<BookedRoom> existingBookings) {
        return existingBookings.stream()
                .filter(existingBooking ->
                        existingBooking.getCheckInDate() != null && existingBooking.getCheckOutDate() != null)
                .noneMatch(existingBooking ->
                        bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckOutDate()) &&
                        bookingRequest.getCheckOutDate().isAfter(existingBooking.getCheckInDate())
                );
    }
}
