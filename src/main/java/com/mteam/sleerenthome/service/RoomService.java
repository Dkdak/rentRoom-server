package com.mteam.sleerenthome.service;

import com.mteam.sleerenthome.exception.InternalServerException;
import com.mteam.sleerenthome.exception.ResourceNotFoundException;
import com.mteam.sleerenthome.model.Room;
import com.mteam.sleerenthome.repository.RoomRepository;
import com.mteam.sleerenthome.utils.ImageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomService implements IRoomService{

    private final RoomRepository roomRepository;

    @Override
    @Transactional
    public Room addNewRoom(MultipartFile file, String roomType, BigDecimal roomPrice) throws SQLException, IOException {

        Room room = new Room();
        room.setRoomType(roomType);
        room.setRoomPrice(roomPrice);
        if(!file.isEmpty()) {

            byte[] photoBytes = ImageUtils.convertToJpgAndResize(file.getInputStream(), 400, 400);
            Blob photoBlob = new SerialBlob(photoBytes);
            room.setPhoto(photoBlob);
        }

        return roomRepository.save(room);
    }


    @Override
    @Transactional(readOnly = true)
    public List<String> getAllRoomTypes() {
        return roomRepository.findDistinctRoomTypes();
    }


    @Override
    @Transactional(readOnly = true)
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }


    @Override
    @Transactional(readOnly = true)
    public List<Room> findAllWithBookings() {
        return roomRepository.findAllWithBookings();
    }


    @Override
    public Optional<byte[]> getRoomPhotoByRoomId(Long roomId) {
        return roomRepository.findById(roomId)
                .map(room -> Optional.ofNullable(room.getPhoto()))
                .orElseThrow(() -> new ResourceNotFoundException("sorry, room not found"))
                .flatMap(this::convertBlobToByteArray);
    }


    @Override
    public Optional<byte[]> getRoomPhotoBypotoBlob(Blob photoBlob) {
        return convertBlobToByteArray(photoBlob);
    }


    private Optional<byte[]> convertBlobToByteArray(Blob photoBlob) {

        if (photoBlob == null) {
            return Optional.empty();
        }

        /*
        try-with-resources 문법
        - InputStream과 ByteArrayOutputStream을 자동으로 관리하고,
        - 블록이 끝날 때 이 리소스들을 자동으로 닫아줍니다.
         이는 자원을 명시적으로 닫아야 하는 번거로움을 덜어주고, 코드의 안전성을 높여줍니다.
        */
        try (InputStream inputStream = photoBlob.getBinaryStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            return Optional.of(outputStream.toByteArray());
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Failed to convert Blob to byte[]", e);
        }
    }


    @Transactional(readOnly = true)
    public Optional<Room> findRoomById(Long roomId) {
        return roomRepository.findById(roomId);
    }

    @Override
    public void deleteRoom(Long roomId) {
        Optional<Room> theRoom = roomRepository.findById(roomId);
        if(theRoom.isPresent()) {
            roomRepository.deleteById(roomId);
        }
    }

    @Override
    public Room updateRoom(Long roomId, String roomType, BigDecimal roomPrice, byte[] photoBytes) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found for this id :: " + roomId));
        if(roomType != null) room.setRoomType(roomType);
        if(roomPrice != null) room.setRoomPrice(roomPrice);
        if(photoBytes != null && photoBytes.length > 0) {
            try {
                photoBytes = ImageUtils.convertToJpgAndResize(new ByteArrayInputStream(photoBytes), 400, 400);
                room.setPhoto(new SerialBlob(photoBytes));
            } catch (SQLException | IOException e) {
                throw new InternalServerException("Error update room");
            }
        }

        return roomRepository.save(room);
    }

    @Override
    public Optional<Room> getRoomById(Long roomId) {
        return roomRepository.findById(roomId);
    }

    @Override
    public List<Room> getAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate, String roomType) {
        return roomRepository.findAvailableRoomsByDatesAndType(checkInDate, checkOutDate, roomType);
    }


}
