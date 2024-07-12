package com.mteam.sleerenthome.controller;

import com.mteam.sleerenthome.exception.PhotoRetrievalException;
import com.mteam.sleerenthome.exception.ResourceNotFoundException;
import com.mteam.sleerenthome.model.BookedRoom;
import com.mteam.sleerenthome.model.Room;
import com.mteam.sleerenthome.respnse.BookingResponse;
import com.mteam.sleerenthome.respnse.RoomResponse;
import com.mteam.sleerenthome.service.IBookingService;
import com.mteam.sleerenthome.service.IRoomService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@CrossOrigin("http://localhost:5173")
@RequiredArgsConstructor
@RequestMapping("/rooms")
public class RoomController {

    private static final Logger logger = LogManager.getLogger(RoomController.class);


    private final IRoomService roomService;
    private final IBookingService bookingService;


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


    /**
     * 이미 가지고 온 데이터를 이용하여 데이터 편집
     * N+1의 문제가 발생하고 있어. 다시말해서 Room에서 Booking정보가 없는경우 있는지 확인하는 쿼리를 던지고 있다
     * JPQL을 사용하여 처리
     */
    @GetMapping("/all-rooms")
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        List<Room> rooms = roomService.findAllWithBookings();

        List<RoomResponse> roomResponses = rooms.stream()
                .map(this::mapToRoomResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(roomResponses);
    }


    private RoomResponse mapToRoomResponse(Room room) {
        byte[] photoBytes = roomService.getRoomPhotoBypotoBlob(room.getPhoto())
                .orElseThrow(() -> new PhotoRetrievalException("Error retrieving photo: " + room.getId()));

        List<BookingResponse> bookingResponses = Optional.ofNullable(room.getBookings())
                .map(bookings -> bookings.stream()
                        .map(booking -> new BookingResponse(
                                booking.getBookingId(),
                                booking.getCheckInDate(),
                                booking.getCheckOutDate(),
                                booking.getBookingConfirmationCode()))
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());

        return new RoomResponse(
                room.getId(),
                room.getRoomType(),
                room.getRoomPrice(),
                room.isBooked(),
                photoBytes, bookingResponses
        );
    }


    /**
     * 기본데이터에서 각각의 정보를 다시 roomRepository를 이용하여 다시 가져옴
     * JPA는 캐시를 사용하는 것으로 DB를 직접 일지 않음으로 속도에는 차이가 없다.
     */
    @GetMapping("/orgin/all-rooms")
    public ResponseEntity<List<RoomResponse>> _getAllRooms() {
        List<Room> rooms = roomService.getAllRooms();
        List<RoomResponse> roomResponses = rooms.stream()
                .map(this::_getRoomResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(roomResponses);
    }


    private RoomResponse _getRoomResponse(Room room) {
        List<BookingResponse> bookingInfo = Optional.ofNullable(room.getBookings())
                .map(bookings -> {
                    // room.getBookings()가 null이 아닌 경우에만 getAllBookingsByRoomId(room.getId())을 호출하여 bookingResponses를 설정
                    List<BookedRoom> bookingsList = getAllBookingsByRoomId(room.getId());
                    return bookingsList.stream()
                            .map(booking -> new BookingResponse(
                                    booking.getBookingId(),
                                    booking.getCheckInDate(),
                                    booking.getCheckOutDate(),
                                    booking.getBookingConfirmationCode()))
                            .collect(Collectors.toList());
                })
                .orElse(Collections.emptyList());

        byte[] photoBytes = roomService.getRoomPhotoByRoomId(room.getId())
                .orElseThrow(() -> new PhotoRetrievalException("Error retrieving photo: " + room.getId()));

        return new RoomResponse(room.getId(),
                room.getRoomType(),
                room.getRoomPrice(),
                room.isBooked(),
                photoBytes, bookingInfo
        );
    }

    private List<BookedRoom> getAllBookingsByRoomId(Long roomId) {
        return bookingService.getAllBookingsByRoomId(roomId);
    }


    /**
     * 기본데이터에서 각각의 정보를 다시 roomRepository를 이용하여 다시 가져옴
     * JPA는 캐시를 사용하는 것으로 DB를 직접 일지 않음으로 속도에는 차이가 없다.
     */
    @GetMapping("/bookings-room/{id}")
    public ResponseEntity<RoomResponse> getBookingsByRoomId(@PathVariable("id") Long roomId) {

        Room room = roomService.findRoomById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found for this id :: " + roomId));

        List<BookingResponse> bookingResponses = getAllBookingsByRoomId(roomId)
                .stream()
                .map(booking -> new BookingResponse(
                        booking.getBookingId(),
                        booking.getCheckInDate(),
                        booking.getCheckOutDate(),
                        booking.getBookingConfirmationCode()))
                .collect(Collectors.toList());

        RoomResponse roomResponse = new RoomResponse(
                room.getId(),
                room.getRoomType(),
                room.getRoomPrice(),
                true, null,
                bookingResponses
        );

        return ResponseEntity.ok(roomResponse);
    }



    @DeleteMapping("/delete/room/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable("roomId") Long roomId) {

        roomService.deleteRoom(roomId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }



    @PutMapping("/update/{roomId}")
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable Long roomId,
                                                   @RequestParam(required = false) String roomType,
                                                   @RequestParam(required = false) BigDecimal roomPrice,
                                                   @RequestParam(required = false) MultipartFile photo) throws IOException, SQLException {
        byte[] photoBytes = photo != null && !photo.isEmpty()?
                photo.getBytes() : roomService.getRoomPhotoByRoomId(roomId).orElseThrow(() -> new PhotoRetrievalException("Error retrieving photo: " + roomId));
        Blob photoBlob = photoBytes != null && photoBytes.length > 0 ? new SerialBlob(photoBytes) : null;

        Room theRoom = roomService.updateRoom(roomId, roomType, roomPrice, photoBytes);
        theRoom.setPhoto(photoBlob);
        RoomResponse roomResponse = _getRoomResponse(theRoom);
        return ResponseEntity.ok(roomResponse);
    }


    @GetMapping("/room/{roomId}")
    public ResponseEntity<Optional<RoomResponse>> getRoomById(@PathVariable Long roomId) {

        logger.info("getRoomById...");
        Optional<Room> theRoom = roomService.getRoomById(roomId);
        return theRoom.map(room -> {
            RoomResponse roomResponse = _getRoomResponse(room);
            return ResponseEntity.ok(Optional.of(roomResponse));
        }).orElseThrow(()-> new ResourceNotFoundException("room not found"));

    }
}

