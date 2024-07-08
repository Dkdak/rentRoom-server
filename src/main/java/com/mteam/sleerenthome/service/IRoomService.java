package com.mteam.sleerenthome.service;

import com.mteam.sleerenthome.exception.InternalServerException;
import com.mteam.sleerenthome.model.Room;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface IRoomService {
    Room addNewRoom(MultipartFile photo, String roomType, BigDecimal roomPrice) throws SQLException, IOException;

    List<String> getAllRoomTypes();

    List<Room> getAllRooms();

    List<Room> findAllWithBookings();

    Optional<byte[]> getRoomPhotoByRoomId(Long roomId);

    Optional<byte[]> getRoomPhotoBypotoBlob(Blob photoBlob);


    Optional<Room> findRoomById(Long roomId);

    void deleteRoom(Long roomId);

    Room updateRoom(Long roomId, String roomType, BigDecimal roomPrice, byte[] photoBytes);

    Optional<Room> getRoomById(Long roomId);
}

