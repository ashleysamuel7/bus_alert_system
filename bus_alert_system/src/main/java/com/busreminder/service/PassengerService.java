package com.busreminder.service;

import com.busreminder.model.BusPassenger;

import java.util.List;

/**
 * Service interface for passenger data operations.
 */
public interface PassengerService {
    
    /**
     * Get all passengers for a given bus ID.
     * 
     * @param busId Bus identifier
     * @return List of passengers on the bus
     */
    List<BusPassenger> getPassengersByBusId(String busId);
    
    /**
     * Get all unnotified passengers for a given bus ID.
     * 
     * @param busId Bus identifier
     * @return List of unnotified passengers on the bus
     */
    List<BusPassenger> getUnnotifiedPassengersByBusId(String busId);
}
