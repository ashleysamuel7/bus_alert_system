package com.busreminder.repository;

import com.busreminder.model.BusPassenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusPassengerRepository extends JpaRepository<BusPassenger, Long> {
    List<BusPassenger> findByPnrIdIn(List<String> pnrIds);
    List<BusPassenger> findByPnrIdInAndNotifiedFalse(List<String> pnrIds);
}

