package com.example.alquila_seguro_backend.repositories;

import com.example.alquila_seguro_backend.entity.Client;
import com.example.alquila_seguro_backend.entity.Property;
import com.example.alquila_seguro_backend.entity.PropertyStatus;
import com.example.alquila_seguro_backend.entity.Reservation;
import com.example.alquila_seguro_backend.entity.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ReservationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReservationRepository reservationRepository;

    private Client client1;
    private Client client2;
    private Property property1;
    private Property property2;
    private Reservation reservation1;
    private Reservation reservation2;
    private Reservation reservation3;

    @BeforeEach
    void setUp() {
        client1 = Client.builder()
                .firstName("Ana")
                .lastName("Gomez")
                .email("ana.gomez@example.com")
                .phone("2234567890")
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(client1);


        client2 = Client.builder()
                .firstName("Pedro")
                .lastName("Lopez")
                .email("pedro.lopez@example.com")
                .phone("2235671221")
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persist(client2);
        Set<String> propertyFeatures = new HashSet<>();
        propertyFeatures.add("Wi-Fi");
        propertyFeatures.add("Parking Gratuito");
        propertyFeatures.add("Piscina");
        propertyFeatures.add("Social Security");
        propertyFeatures.add("GARAGE EX");

        property1 = Property.builder()
                .title("Apartamento Centro")
                .description("Moderno apartamento en el centro")
                .location("Buenos Aires")
                .pricePerNight(100.0)
                .imageUrl("https://i.redd.it/so-how-we-feeling-about-the-new-ll-ssj-goku-v0-hrb1t0sez1lb1.jpg?width=800&format=pjpg&auto=webp&s=055e47daf8383d6e031f4dc87c5b40132aa0f298")
                .category("Apartamento")
                .features(propertyFeatures)
                .propertyStatus(PropertyStatus.AVAILABLE)
                .build();
        entityManager.persist(property1);

        property2 = Property.builder()
                .title("Cabaña en la montaña")
                .description("Acogedora cabaña con vistas")
                .location("Bariloche")
                .pricePerNight(200.0)
                .category("Cabaña")
                .imageUrl("https://i.redd.it/so-how-we-feeling-about-the-new-ll-ssj-goku-v0-hrb1t0sez1lb1.jpg?width=800&format=pjpg&auto=webp&s=055e47daf8383d6e031f4dc87c5b40132aa0f298")
                .features(propertyFeatures)
                .propertyStatus(PropertyStatus.AVAILABLE)
                .build();
        entityManager.persist(property2);

        reservation1 = Reservation.builder()
                .client(client1)
                .property(property1)
                .startDate(LocalDateTime.of(2025, 6, 1, 10, 0))
                .endDate(LocalDateTime.of(2025, 6, 5, 10, 0))
                .status(ReservationStatus.CONFIRMED)
                .build();
        entityManager.persist(reservation1);

        reservation2 = Reservation.builder()
                .client(client1)
                .property(property2)
                .startDate(LocalDateTime.of(2025, 7, 10, 10, 0))
                .endDate(LocalDateTime.of(2025, 7, 15, 10, 0))
                .status(ReservationStatus.PENDING)
                .build();
        entityManager.persist(reservation2);

        reservation3 = Reservation.builder()
                .client(client2)
                .property(property1)
                .startDate(LocalDateTime.of(2025, 8, 1, 10, 0))
                .endDate(LocalDateTime.of(2025, 8, 5, 10, 0))
                .status(ReservationStatus.CANCELLED) // Estado CANCELLED para solapamiento
                .build();
        entityManager.persist(reservation3);

        entityManager.flush(); // Asegura que los datos se persistan antes de las consultas
    }

    @Test
    void testFindByClientId() {
        List<Reservation> reservations = reservationRepository.findByClientId(client1.getId());
        assertNotNull(reservations);
        assertEquals(2, reservations.size());
        assertTrue(reservations.stream().anyMatch(r -> r.getId().equals(reservation1.getId())));
        assertTrue(reservations.stream().anyMatch(r -> r.getId().equals(reservation2.getId())));
    }

    @Test
    void testFindByPropertyId() {
        List<Reservation> reservations = reservationRepository.findByPropertyId(property1.getId());
        assertNotNull(reservations);
        assertEquals(2, reservations.size());
        assertTrue(reservations.stream().anyMatch(r -> r.getId().equals(reservation1.getId())));
        assertTrue(reservations.stream().anyMatch(r -> r.getId().equals(reservation3.getId())));
    }

    @Test
    void testFindByStatus() {
        List<Reservation> pendingReservations = reservationRepository.findByStatus(ReservationStatus.PENDING);
        assertNotNull(pendingReservations);
        assertEquals(1, pendingReservations.size());
        assertEquals(reservation2.getId(), pendingReservations.get(0).getId());

        List<Reservation> confirmedReservations = reservationRepository.findByStatus(ReservationStatus.CONFIRMED);
        assertNotNull(confirmedReservations);
        assertEquals(1, confirmedReservations.size());
        assertEquals(reservation1.getId(), confirmedReservations.get(0).getId());
    }

    @Test
    void testFindOverlappingReservations_OverlappingWithConfirmed() {
        // Solapamiento con reservation1 (01/06-05/06 CONFIRMED)
        LocalDateTime checkStartDate = LocalDateTime.of(2025, 6, 3, 10, 0);
        LocalDateTime checkEndDate = LocalDateTime.of(2025, 6, 7, 10, 0);
        List<Reservation> overlaps = reservationRepository.findOverlappingReservations(property1.getId(), checkStartDate, checkEndDate);
        assertNotNull(overlaps);
        assertEquals(1, overlaps.size());
        assertEquals(reservation1.getId(), overlaps.get(0).getId());
    }

    @Test
    void testFindOverlappingReservations_NoOverlap() {
        // Fechas que no se solapan con ninguna reserva de property1
        LocalDateTime checkStartDate = LocalDateTime.of(2025, 6, 6, 10, 0);
        LocalDateTime checkEndDate = LocalDateTime.of(2025, 6, 9, 10, 0);
        List<Reservation> overlaps = reservationRepository.findOverlappingReservations(property1.getId(), checkStartDate, checkEndDate);
        assertNotNull(overlaps);
        assertTrue(overlaps.isEmpty());
    }

    @Test
    void testFindOverlappingReservations_OverlapWithCancelledAreIgnored() {
        // Solapamiento con reservation3 (01/08-05/08 CANCELLED)
        LocalDateTime checkStartDate = LocalDateTime.of(2025, 8, 2, 10, 0);
        LocalDateTime checkEndDate = LocalDateTime.of(2025, 8, 4, 10, 0);
        List<Reservation> overlaps = reservationRepository.findOverlappingReservations(property1.getId(), checkStartDate, checkEndDate);
        assertNotNull(overlaps);
        assertTrue(overlaps.isEmpty()); // Debería estar vacía porque CANCELLED se ignora
    }

    @Test
    void testFindOverlappingReservations_EdgeCase_StartsAtExistingReservationEnd() {
        // Empieza justo cuando termina reservation1 (01/06-05/06)
        LocalDateTime checkStartDate = LocalDateTime.of(2025, 6, 5, 10, 0);
        LocalDateTime checkEndDate = LocalDateTime.of(2025, 6, 10, 10, 0);
        List<Reservation> overlaps = reservationRepository.findOverlappingReservations(property1.getId(), checkStartDate, checkEndDate);
        assertNotNull(overlaps);
        assertFalse(overlaps.isEmpty()); // Debería solaparse por startDate <= endDate
        assertEquals(1, overlaps.size());
        assertEquals(reservation1.getId(), overlaps.get(0).getId());
    }

    @Test
    void testFindOverlappingReservations_EdgeCase_EndsAtExistingReservationStart() {
        // Termina justo cuando empieza reservation1 (01/06-05/06)
        LocalDateTime checkStartDate = LocalDateTime.of(2025, 5, 28, 10, 0);
        LocalDateTime checkEndDate = LocalDateTime.of(2025, 6, 1, 10, 0);
        List<Reservation> overlaps = reservationRepository.findOverlappingReservations(property1.getId(), checkStartDate, checkEndDate);
        assertNotNull(overlaps);
        assertFalse(overlaps.isEmpty()); // Debería solaparse por endDate >= startDate
        assertEquals(1, overlaps.size());
        assertEquals(reservation1.getId(), overlaps.get(0).getId());
    }

}