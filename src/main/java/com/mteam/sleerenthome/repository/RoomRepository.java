package com.mteam.sleerenthome.repository;

import com.mteam.sleerenthome.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    @Query("select distinct r.roomType from Room r")
    List<String> findDistinctRoomTypes();

    @Query("SELECT DISTINCT r FROM Room r LEFT JOIN FETCH r.bookings")
    List<Room> findAllWithBookings();

    @Query("SELECT r FROM Room r " +
            "WHERE r.roomType LIKE %:roomType% " +
            "AND r.id NOT IN (" +
            " SELECT br.room.id FROM BookedRoom br " +
            " WHERE (br.checkInDate <= :checkOutDate AND br.checkOutDate >= :checkInDate)" +
            ")")
    List<Room> findAvailableRoomsByDatesAndType(LocalDate checkInDate, LocalDate checkOutDate, String roomType);

}
