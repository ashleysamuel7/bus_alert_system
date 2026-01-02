package com.busreminder.repository;

import com.busreminder.model.BusPnr;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class BusPnrRepositoryTest {

    @Autowired
    private BusPnrRepository busPnrRepository;

    @Test
    void testSaveAndFindByBusId() {
        // Given
        BusPnr busPnr1 = new BusPnr();
        busPnr1.setBusId("BUS001");
        busPnr1.setPnrId("PNR001");
        busPnrRepository.save(busPnr1);

        BusPnr busPnr2 = new BusPnr();
        busPnr2.setBusId("BUS001");
        busPnr2.setPnrId("PNR002");
        busPnrRepository.save(busPnr2);

        BusPnr busPnr3 = new BusPnr();
        busPnr3.setBusId("BUS002");
        busPnr3.setPnrId("PNR003");
        busPnrRepository.save(busPnr3);

        // When
        List<BusPnr> result = busPnrRepository.findByBusId("BUS001");

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(pnr -> pnr.getBusId().equals("BUS001")));
        assertTrue(result.stream().allMatch(pnr -> pnr.getCreatedAt() != null));
    }

    @Test
    void testFindByBusId_WhenNoRecords() {
        // When
        List<BusPnr> result = busPnrRepository.findByBusId("BUS999");

        // Then
        assertTrue(result.isEmpty());
    }
}
