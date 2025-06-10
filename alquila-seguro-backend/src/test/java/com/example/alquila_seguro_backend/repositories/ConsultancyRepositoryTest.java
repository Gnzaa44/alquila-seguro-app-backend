package com.example.alquila_seguro_backend.repositories;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import com.example.alquila_seguro_backend.entity.Client;
import com.example.alquila_seguro_backend.entity.Consultancy;
import com.example.alquila_seguro_backend.entity.ConsultancyStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest // Anotación clave para tests de repositorios JPA
public class ConsultancyRepositoryTest {
    @Autowired
    private ConsultancyRepository consultancyRepository;

    @Autowired
    private TestEntityManager entityManager; // Utilidad para persistir datos de prueba directamente en la DB de test

    private Client testClient;
    private Consultancy pendingConsultancy;
    private Consultancy respondedConsultancy;
    private Consultancy closedConsultancy;

    @BeforeEach
    void setUp() {
        // Asegúrate de persistir un cliente para asociar las consultorías
        testClient = Client.builder()
                .firstName("Test")
                .lastName("Client")
                .email("test@gmail.com")
                .phone("2235890912")
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(testClient); // Guarda el cliente en la DB de test

        // Configuración de consultorías con los estados correctos
        pendingConsultancy = Consultancy.builder()
                .client(testClient)
                .details("Details for pending consultancy")
                .requestedAt(LocalDateTime.now())
                .status(ConsultancyStatus.PENDING)
                .build();
        entityManager.persistAndFlush(pendingConsultancy);

        respondedConsultancy = Consultancy.builder()
                .client(testClient)
                .details("Details for responded consultancy")
                .requestedAt(LocalDateTime.now().minusDays(1))
                .status(ConsultancyStatus.RESPONDED) // Estado RESPONDED
                .build();
        entityManager.persistAndFlush(respondedConsultancy);

        closedConsultancy = Consultancy.builder()
                .client(testClient)
                .details("Details for closed consultancy")
                .requestedAt(LocalDateTime.now().minusDays(2))
                .status(ConsultancyStatus.CLOSED) // Estado CLOSED
                .build();
        entityManager.persistAndFlush(closedConsultancy);
    }

    @Test
    void testFindById() {
        Optional<Consultancy> foundConsultancy = consultancyRepository.findById(pendingConsultancy.getId());
        assertTrue(foundConsultancy.isPresent());
        assertEquals(pendingConsultancy.getDetails(), foundConsultancy.get().getDetails());
    }

    @Test
    void testFindById_NotFound() {
        Optional<Consultancy> foundConsultancy = consultancyRepository.findById(999L); // ID que no existe
        assertFalse(foundConsultancy.isPresent());
    }

    @Test
    void testSaveConsultancy() {
        Consultancy newConsultancy = Consultancy.builder()
                .client(testClient)
                .details("New consultancy details")
                .requestedAt(LocalDateTime.now())
                .status(ConsultancyStatus.PENDING) // Siempre se guardan como PENDING inicialmente
                .build();

        Consultancy savedConsultancy = consultancyRepository.save(newConsultancy);

        assertNotNull(savedConsultancy.getId()); // Verifica que se le asignó un ID
        assertEquals("New consultancy details", savedConsultancy.getDetails());
        assertEquals(ConsultancyStatus.PENDING, savedConsultancy.getStatus());

        // Opcional: recuperarla de nuevo para verificar persistencia real
        Optional<Consultancy> found = consultancyRepository.findById(savedConsultancy.getId());
        assertTrue(found.isPresent());
        assertEquals(savedConsultancy.getId(), found.get().getId());
    }

    @Test
    void testFindByClientId() {
        List<Consultancy> consultancies = consultancyRepository.findByClientId(testClient.getId());
        assertNotNull(consultancies);
        assertEquals(3, consultancies.size()); // Ahora esperamos 3 consultorías
        assertTrue(consultancies.stream().anyMatch(c -> c.getId().equals(pendingConsultancy.getId())));
        assertTrue(consultancies.stream().anyMatch(c -> c.getId().equals(respondedConsultancy.getId())));
        assertTrue(consultancies.stream().anyMatch(c -> c.getId().equals(closedConsultancy.getId())));
    }

    @Test
    void testFindByStatus_Pending() {
        List<Consultancy> foundConsultancies = consultancyRepository.findByStatus(ConsultancyStatus.PENDING);
        assertNotNull(foundConsultancies);
        assertEquals(1, foundConsultancies.size());
        assertEquals(pendingConsultancy.getId(), foundConsultancies.getFirst().getId());
    }

    @Test
    void testFindByStatus_Responded() {
        List<Consultancy> foundConsultancies = consultancyRepository.findByStatus(ConsultancyStatus.RESPONDED);
        assertNotNull(foundConsultancies);
        assertEquals(1, foundConsultancies.size());
        assertEquals(respondedConsultancy.getId(), foundConsultancies.getFirst().getId());
    }

    @Test
    void testFindByStatus_Closed() {
        List<Consultancy> foundConsultancies = consultancyRepository.findByStatus(ConsultancyStatus.CLOSED);
        assertNotNull(foundConsultancies);
        assertEquals(1, foundConsultancies.size());
        assertEquals(closedConsultancy.getId(), foundConsultancies.getFirst().getId());
    }


    @Test
    void testDeleteConsultancy() {
        consultancyRepository.deleteById(pendingConsultancy.getId());
        Optional<Consultancy> deletedConsultancy = consultancyRepository.findById(pendingConsultancy.getId());
        assertFalse(deletedConsultancy.isPresent());
    }
}
