package com.busreminder.service;

import com.busreminder.model.BusPnr;
import com.busreminder.model.BusPassenger;
import com.busreminder.repository.BusPnrRepository;
import com.busreminder.repository.BusPassengerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PassengerService {

    private final BusPnrRepository busPnrRepository;
    private final BusPassengerRepository busPassengerRepository;

    public PassengerService(BusPnrRepository busPnrRepository, 
                           BusPassengerRepository busPassengerRepository) {
        this.busPnrRepository = busPnrRepository;
        this.busPassengerRepository = busPassengerRepository;
    }

    public List<BusPassenger> getPassengersByBusId(String busId) {
        List<BusPnr> pnrs = busPnrRepository.findByBusId(busId);
        List<String> pnrIds = pnrs.stream()
                .map(BusPnr::getPnrId)
                .collect(Collectors.toList());
        
        if (pnrIds.isEmpty()) {
            return List.of();
        }
        
        return busPassengerRepository.findByPnrIdIn(pnrIds);
    }

    public List<BusPassenger> getUnnotifiedPassengersByBusId(String busId) {
        List<BusPnr> pnrs = busPnrRepository.findByBusId(busId);
        List<String> pnrIds = pnrs.stream()
                .map(BusPnr::getPnrId)
                .collect(Collectors.toList());
        
        if (pnrIds.isEmpty()) {
            return List.of();
        }
        
        return busPassengerRepository.findByPnrIdInAndNotifiedFalse(pnrIds);
    }
}

