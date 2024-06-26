package com.mteam.sleerenthome.controller;

import com.mteam.sleerenthome.exception.PhotoRetrievalException;
import com.mteam.sleerenthome.model.BookedRoom;
import com.mteam.sleerenthome.model.Room;
import com.mteam.sleerenthome.respnse.BookingResponse;
import com.mteam.sleerenthome.respnse.RoomResponse;
import com.mteam.sleerenthome.service.BookingService;
import com.mteam.sleerenthome.service.IRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin("http://localhost:5173")
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class RoomController {

    private final IRoomService roomService;

    private final BookingService bookingService;


    @PostMapping("/add/new-room")
    public ResponseEntity<RoomResponse> addNewRoom(
            @RequestParam("photo") MultipartFile photo,
            @RequestParam("roomType") String roomType,
            @RequestParam("roomPrice") BigDecimal roomPrice)  throws SQLException, IOException {

        Room savedRoom = roomService.addNewRoom(photo, roomType, roomPrice);
        RoomResponse response = new RoomResponse(savedRoom.getId(), savedRoom.getRoomType(), savedRoom.getRoomPrice());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/room/room-types")
    public List<String> getRoomTypes() {
        return roomService.getAllRoomTypes();
    }




    @GetMapping("/all-rooms")
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        List<Room> rooms = roomService.getAllRooms();

        List<RoomResponse> roomResponses = rooms.stream()
                .map(this::mapToRoomResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(roomResponses);
    }

    private RoomResponse mapToRoomResponse(Room room) {
        // Convert Blob photo to byte[]
//        byte[] photoBytes = null;
        byte[] photoBytes = convertBlobToBytes(room.getPhoto());


        return new RoomResponse(
                room.getId(),
                room.getRoomType(),
                room.getRoomPrice(),
                room.isBooked(),
                photoBytes
        );

        // Map bookings to BookingResponse list booking 만들어지면 주석 풀것
//        List<BookingResponse> bookingResponses = (room.getBookings() != null)
//                ? room.getBookings().stream()
//                .map(booking -> new BookingResponse(
//                        booking.getBookingId(),
//                        booking.getCheckInDate(),
//                        booking.getCheckOutDate(),
//                        booking.getBookingConfirmationCode()))
//                .collect(Collectors.toList())
//                : Collections.emptyList();
//
//        return new RoomResponse(
//                room.getId(),
//                room.getRoomType(),
//                room.getRoomPrice(),
//                room.isBooked(),
//                photoBytes,
//                bookingResponses
//        );
    }


    private byte[] convertBlobToBytes(Blob blob) {

        try (InputStream inputStream = blob.getBinaryStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return outputStream.toByteArray();
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Failed to convert Blob to byte[]", e);
        }

    }




    /**
     * 오리지널 코드
     */
    @GetMapping("/orgin/all-rooms")
    public ResponseEntity<List<RoomResponse>> _getAllRooms() throws SQLException {
        List<Room> rooms = roomService.getAllRooms();
        List<RoomResponse> roomResponses = new ArrayList<>();

        for(Room room : rooms) {

            byte[] photoBytes = roomService.getRoomPhotoByRoomId(room.getId());
            if(photoBytes != null && photoBytes.length > 0) {
                String base64Photo = Base64.getEncoder().encodeToString(photoBytes);
                RoomResponse roomResponse = _getRoomResponse(room);
                roomResponse.setPhoto(base64Photo);
                roomResponses.add(roomResponse);
            }
        }

        return ResponseEntity.ok(roomResponses);
    }


    /**
     * 오리지널 코드
     */
    private RoomResponse _getRoomResponse(Room room) {
        List<BookedRoom> bookings = getAllBookingsByRoomId(room.getId());

        List<BookingResponse> bookingInfo = bookings
                .stream()
                .map(booking -> new  BookingResponse(
                        booking.getBookingId(),
                        booking.getCheckInDate(),
                        booking.getCheckOutDate(),
                        booking.getBookingConfirmationCode())).toList();

        byte[] photoBytes = null;
        Blob photoBlob = room.getPhoto();
        if(photoBlob != null) {
            try {
                photoBytes = photoBlob.getBytes(1, (int)photoBlob.length());
            } catch (SQLException e) {
                throw new PhotoRetrievalException("Error retrieving photo");
            }
        }
        return new RoomResponse(room.getId(),
                room.getRoomType(),
                room.getRoomPrice(),
                room.isBooked(), photoBytes, bookingInfo);

    }

    private List<BookedRoom> getAllBookingsByRoomId(Long roomId) {
        return bookingService.getAllBookingsByRoomId(roomId);
    }
}

