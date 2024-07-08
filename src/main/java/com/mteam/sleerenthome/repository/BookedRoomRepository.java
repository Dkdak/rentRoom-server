package com.mteam.sleerenthome.repository;

import com.mteam.sleerenthome.model.BookedRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookedRoomRepository extends JpaRepository<BookedRoom, Long> {

    List<BookedRoom> findAllByRoomId(Long roomId);
}
