package com.busreminder.repository;

import com.busreminder.model.BusPnr;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusPnrRepository extends JpaRepository<BusPnr, Long> {
    List<BusPnr> findByBusId(String busId);
}

