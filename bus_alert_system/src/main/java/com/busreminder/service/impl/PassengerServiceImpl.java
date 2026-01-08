package com.busreminder.service.impl;

import com.busreminder.model.BusPnr;
import com.busreminder.model.BusPassenger;
import com.busreminder.repository.BusPnrRepository;
import com.busreminder.repository.BusPassengerRepository;
import com.busreminder.service.PassengerService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PassengerServiceImpl implements PassengerService {

    private final BusPnrRepository busPnrRepository;
    private final BusPassengerRepository busPassengerRepository;

    public PassengerServiceImpl(BusPnrRepository busPnrRepository, 
                               BusPassengerRepository busPassengerRepository) {
        this.busPnrRepository = busPnrRepository;
        this.busPassengerRepository = busPassengerRepository;
    }

    @Override
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

    @Override
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

