package com.mteam.sleerenthome.service;

import com.mteam.sleerenthome.model.BookedRoom;
import com.mteam.sleerenthome.repository.BookedRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    @Autowired
    private final BookedRoomRepository bookedRoomRepository;

    @Transactional(readOnly = true)
    public List<BookedRoom> getAllBookingsByRoomId(Long roomId) {
        return bookedRoomRepository.findAllByRoomId(roomId);
    }
}
