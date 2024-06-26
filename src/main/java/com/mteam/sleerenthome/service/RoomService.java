package com.mteam.sleerenthome.service;

import com.mteam.sleerenthome.exception.ResourceNotFoundException;
import com.mteam.sleerenthome.model.Room;
import com.mteam.sleerenthome.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
            byte[] photoBytes = file.getBytes();
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
        List<Room> rooms = roomRepository.findAll();
        rooms.forEach(room -> {
            // Ensure photoBase64 is populated if photo is not null
            if (room.getPhoto() != null) {
                room.setPhotoBase64(convertBlobToBase64(room.getPhoto()));
            }
        });
        return rooms;

    }

    @Override
    public byte[] getRoomPhotoByRoomId(Long roomId) throws SQLException {
        Optional<Room> theRoom = roomRepository.findById(roomId);
        if(theRoom.isEmpty()) {
            throw new ResourceNotFoundException("sorry, room not found");
        }
        Blob photoBlob = theRoom.get().getPhoto();

        if (photoBlob == null) {
            return null;
        }

        try (InputStream inputStream = photoBlob.getBinaryStream();
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


//        if(photoBlob != null) {
//
//            try {
//                long blobLength = photoBlob.length();
//                if (blobLength > Integer.MAX_VALUE) {
//                    throw new RuntimeException("Blob size is too large to process.");
//                }
//                return photoBlob.getBytes(1, (int) blobLength);
//            } catch (SQLException e) {
//                throw new RuntimeException("Failed to convert Blob to byte[]", e);
//            }
//
//        }

    }


    private String convertBlobToBase64(Blob blob) {
        try {
            byte[] bytes = blob.getBytes(1, (int) blob.length());
            return Base64.getEncoder().encodeToString(bytes);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
}
