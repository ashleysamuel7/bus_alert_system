package com.busreminder.config;

import com.busreminder.model.BusPnr;
import com.busreminder.model.BusPassenger;
import com.busreminder.repository.BusPnrRepository;
import com.busreminder.repository.BusPassengerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Component
@ConditionalOnProperty(name = "data.loader.enabled", havingValue = "true", matchIfMissing = false)
public class DataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    private final BusPnrRepository busPnrRepository;
    private final BusPassengerRepository busPassengerRepository;
    private final Random random = new Random();

    // NYC area coordinates for pickup locations
    private static final double BASE_LATITUDE = 40.7128;
    private static final double BASE_LONGITUDE = -74.0060;
    private static final double LATITUDE_VARIATION = 0.1; // ~11km
    private static final double LONGITUDE_VARIATION = 0.1; // ~8km

    private static final List<String> FIRST_NAMES = Arrays.asList(
        "John", "Jane", "Michael", "Sarah", "David", "Emily", "Robert", "Jessica",
        "William", "Amanda", "James", "Lisa", "Richard", "Jennifer", "Joseph", "Michelle",
        "Thomas", "Ashley", "Christopher", "Melissa", "Daniel", "Nicole", "Matthew", "Stephanie"
    );

    private static final List<String> LAST_NAMES = Arrays.asList(
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
        "Rodriguez", "Martinez", "Hernandez", "Lopez", "Wilson", "Anderson", "Thomas", "Taylor",
        "Moore", "Jackson", "Martin", "Lee", "Thompson", "White", "Harris", "Clark"
    );

    private static final List<String> STREET_NAMES = Arrays.asList(
        "Main St", "Park Ave", "Broadway", "5th Ave", "Madison Ave", "Lexington Ave",
        "Central Park West", "Columbus Ave", "Amsterdam Ave", "West End Ave", "Riverside Dr",
        "1st St", "2nd St", "3rd St", "Washington St", "Lincoln Ave", "Oak St", "Elm St"
    );

    public DataLoader(BusPnrRepository busPnrRepository, BusPassengerRepository busPassengerRepository) {
        this.busPnrRepository = busPnrRepository;
        this.busPassengerRepository = busPassengerRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if data already exists
        if (busPnrRepository.count() > 0) {
            logger.info("Data already exists. Skipping data load.");
            return;
        }

        logger.info("Starting data load: 10 buses with 20 passengers each");

        for (int busNumber = 1; busNumber <= 10; busNumber++) {
            String busId = String.format("BUS%03d", busNumber);
            loadBusData(busId);
        }

        logger.info("Data load completed. Total buses: {}, Total PNRs: {}, Total passengers: {}",
                busPnrRepository.count() > 0 ? 10 : 0,
                busPnrRepository.count(),
                busPassengerRepository.count());
    }

    private void loadBusData(String busId) {
        // Each bus has 2-4 PNRs (randomized)
        int numPnrs = 2 + random.nextInt(3); // 2, 3, or 4 PNRs
        int totalPassengers = 20;
        int[] passengersPerPnr = distributePassengers(totalPassengers, numPnrs);

        for (int pnrIndex = 1; pnrIndex <= numPnrs; pnrIndex++) {
            String pnrId = String.format("PNR%03d_%d", Integer.parseInt(busId.substring(3)), pnrIndex);
            int passengerCount = passengersPerPnr[pnrIndex - 1];

            // Create BusPnr
            BusPnr busPnr = new BusPnr();
            busPnr.setBusId(busId);
            busPnr.setPnrId(pnrId);
            busPnrRepository.save(busPnr);

            // Create passengers for this PNR
            for (int passengerIndex = 1; passengerIndex <= passengerCount; passengerIndex++) {
                BusPassenger passenger = createPassenger(pnrId, busId, passengerIndex);
                busPassengerRepository.save(passenger);
            }
        }
    }

    private BusPassenger createPassenger(String pnrId, String busId, int passengerIndex) {
        BusPassenger passenger = new BusPassenger();

        String passengerId = String.format("%s_%s_PASS%02d", busId, pnrId, passengerIndex);
        passenger.setPnrId(pnrId);
        passenger.setPassengerId(passengerId);

        // Generate realistic name
        String firstName = FIRST_NAMES.get(random.nextInt(FIRST_NAMES.size()));
        String lastName = LAST_NAMES.get(random.nextInt(LAST_NAMES.size()));
        passenger.setPassengerName(firstName + " " + lastName);

        // Generate phone number
        int phoneNumber = 1234567890 + (Integer.parseInt(busId.substring(3)) * 100) + passengerIndex;
        passenger.setPassengerPhone("+1" + phoneNumber);

        // Generate pickup location (varied around NYC area)
        double latitude = BASE_LATITUDE + (random.nextDouble() - 0.5) * LATITUDE_VARIATION;
        double longitude = BASE_LONGITUDE + (random.nextDouble() - 0.5) * LONGITUDE_VARIATION;
        passenger.setPickupLatitude(BigDecimal.valueOf(latitude).setScale(8, java.math.RoundingMode.HALF_UP));
        passenger.setPickupLongitude(BigDecimal.valueOf(longitude).setScale(8, java.math.RoundingMode.HALF_UP));

        // Generate address
        int streetNumber = 100 + random.nextInt(900);
        String streetName = STREET_NAMES.get(random.nextInt(STREET_NAMES.size()));
        passenger.setPickupAddress(streetNumber + " " + streetName + ", New York, NY");

        // Set notified to false initially
        passenger.setNotified(false);

        return passenger;
    }

    private int[] distributePassengers(int totalPassengers, int numPnrs) {
        int[] distribution = new int[numPnrs];
        int remaining = totalPassengers;

        // Distribute passengers somewhat evenly with slight variation
        int basePerPnr = totalPassengers / numPnrs;
        for (int i = 0; i < numPnrs - 1; i++) {
            // Each PNR gets base amount ± 2 passengers
            distribution[i] = basePerPnr + random.nextInt(5) - 2;
            remaining -= distribution[i];
        }
        // Last PNR gets the remainder
        distribution[numPnrs - 1] = remaining;

        return distribution;
    }
}

