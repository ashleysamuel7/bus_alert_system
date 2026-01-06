package com.busreminder.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class BusPnrTest {

    @Autowired
    private org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager entityManager;

    @Test
    void testOnCreate_SetsCreatedAt() {
        // Given
        BusPnr busPnr = new BusPnr();
        busPnr.setBusId("BUS001");
        busPnr.setPnrId("PNR001");

        // When
        entityManager.persist(busPnr);
        entityManager.flush();
        entityManager.clear();

        // Then
        BusPnr saved = entityManager.find(BusPnr.class, busPnr.getId());
        assertNotNull(saved.getCreatedAt());
        assertTrue(saved.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void testGettersAndSetters() {
        // Given
        BusPnr busPnr = new BusPnr();
        Long id = 1L;
        String busId = "BUS001";
        String pnrId = "PNR001";
        LocalDateTime createdAt = LocalDateTime.now();

        // When
        busPnr.setId(id);
        busPnr.setBusId(busId);
        busPnr.setPnrId(pnrId);
        busPnr.setCreatedAt(createdAt);

        // Then
        assertEquals(id, busPnr.getId());
        assertEquals(busId, busPnr.getBusId());
        assertEquals(pnrId, busPnr.getPnrId());
        assertEquals(createdAt, busPnr.getCreatedAt());
    }
}

