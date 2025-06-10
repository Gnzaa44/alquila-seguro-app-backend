package com.example.alquila_seguro_backend.services;
import com.example.alquila_seguro_backend.dto.*;
import com.example.alquila_seguro_backend.entity.*;
import com.example.alquila_seguro_backend.mercadopago.MercadoPagoService;
import com.example.alquila_seguro_backend.repositories.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import static org.mockito.Mockito.mockConstruction;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.eq;


import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @InjectMocks
    private ReservationService reservationService;

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private PropertyRepository propertyRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private ContractRepository contractRepository;

    private Reservation testReservation;
    private Client testClient;
    private Property testProperty;
    private Invoice testInvoice;
    private Contract testContract;
    private ReservationCreateRequest reservationCreateRequest;
    private com.example.alquila_seguro_backend.entity.Payment testPayment;

    @BeforeEach
    void setUp() {
        // Inicializa tus objetos de prueba aquí
        testClient = Client.builder().id(1L).email("test@example.com").firstName("John").lastName("Doe").phone("123456789").build();
        testProperty = Property.builder()
                .id(1L)
                .title("Test Property")
                .description("Description")
                .pricePerNight(1000.0)
                .propertyStatus(PropertyStatus.AVAILABLE)
                .build();
        testReservation = Reservation.builder()
                .id(1L)
                .property(testProperty)
                .client(testClient)
                .contract(testContract)
                .invoice(testInvoice)
                .startDate(LocalDateTime.of(2025, 8, 1, 10, 0))
                .endDate(LocalDateTime.of(2025, 8, 5, 10, 0))
                .status(ReservationStatus.PENDING) // Importante: empieza PENDING
                .build();

        testInvoice = Invoice.builder().id(1L).status(DocumentStatus.PENDING).build();
        testContract = Contract.builder().id(1L).status(DocumentStatus.PENDING).build();


    }

    // --- Tests para getAllReservations ---
    @Test
    void testGetAllReservations_Success() {
        when(reservationRepository.findAll()).thenReturn(Arrays.asList(testReservation));

        ApiResponse<List<ReservationResponse>> response = reservationService.getAllReservations();

        assertTrue(response.isSuccess());
        assertEquals("Reservas recuperadas correctamente.", response.getMessage());
        assertFalse(response.getData().isEmpty());
        assertEquals(1, response.getData().size());
        assertEquals(testReservation.getId(), response.getData().get(0).getId());
        verify(reservationRepository, times(1)).findAll();
    }

    @Test
    void testGetAllReservations_NoContent() {
        when(reservationRepository.findAll()).thenReturn(Collections.emptyList());

        ApiResponse<List<ReservationResponse>> response = reservationService.getAllReservations();

        assertTrue(response.isSuccess());
        assertEquals("Reservas recuperadas correctamente.", response.getMessage());
        assertTrue(response.getData().isEmpty());
        verify(reservationRepository, times(1)).findAll();
    }

    // --- Tests para getReservationsByClientId ---
    @Test
    void testGetReservationsByClientId_Success() {
        when(reservationRepository.findByClientId(testClient.getId())).thenReturn(Arrays.asList(testReservation));

        ApiResponse<List<ReservationResponse>> response = reservationService.getReservationsByClientId(testClient.getId());

        assertTrue(response.isSuccess());
        assertEquals("Reservas por cliente recuperadas correctamente.", response.getMessage());
        assertFalse(response.getData().isEmpty());
        assertEquals(1, response.getData().size());
        assertEquals(testReservation.getId(), response.getData().get(0).getId());
        verify(reservationRepository, times(1)).findByClientId(testClient.getId());
    }

    @Test
    void testGetReservationsByClientId_NoContent() {
        when(reservationRepository.findByClientId(anyLong())).thenReturn(Collections.emptyList());

        ApiResponse<List<ReservationResponse>> response = reservationService.getReservationsByClientId(99L);

        assertTrue(response.isSuccess());
        assertEquals("Reservas por cliente recuperadas correctamente.", response.getMessage());
        assertTrue(response.getData().isEmpty());
        verify(reservationRepository, times(1)).findByClientId(99L);
    }

    // --- Tests para getReservationsByPropertyId ---
    @Test
    void testGetReservationsByPropertyId_Success() {
        when(reservationRepository.findByPropertyId(testProperty.getId())).thenReturn(Arrays.asList(testReservation));

        ApiResponse<List<ReservationResponse>> response = reservationService.getReservationsByPropertyId(testProperty.getId());

        assertTrue(response.isSuccess());
        assertEquals("Reservas por propiedad recuperadas correctamente.", response.getMessage());
        assertFalse(response.getData().isEmpty());
        assertEquals(1, response.getData().size());
        assertEquals(testReservation.getId(), response.getData().get(0).getId());
        verify(reservationRepository, times(1)).findByPropertyId(testProperty.getId());
    }

    @Test
    void testGetReservationsByPropertyId_NoContent() {
        when(reservationRepository.findByPropertyId(anyLong())).thenReturn(Collections.emptyList());

        ApiResponse<List<ReservationResponse>> response = reservationService.getReservationsByPropertyId(99L);

        assertTrue(response.isSuccess());
        assertEquals("Reservas por propiedad recuperadas correctamente.", response.getMessage());
        assertTrue(response.getData().isEmpty());
        verify(reservationRepository, times(1)).findByPropertyId(99L);
    }

    // --- Tests para getReservationsByStatus ---
    @Test
    void testGetReservationsByStatus_Success() {
        when(reservationRepository.findByStatus(ReservationStatus.PENDING)).thenReturn(Arrays.asList(testReservation));

        ApiResponse<List<ReservationResponse>> response = reservationService.getReservationsByStatus(ReservationStatus.PENDING);

        assertTrue(response.isSuccess());
        assertEquals("Reservas por estado recuperadas correctamente.", response.getMessage());
        assertFalse(response.getData().isEmpty());
        assertEquals(1, response.getData().size());
        assertEquals(testReservation.getId(), response.getData().get(0).getId());
        verify(reservationRepository, times(1)).findByStatus(ReservationStatus.PENDING);
    }

    @Test
    void testGetReservationsByStatus_NoContent() {
        when(reservationRepository.findByStatus(any(ReservationStatus.class))).thenReturn(Collections.emptyList());

        ApiResponse<List<ReservationResponse>> response = reservationService.getReservationsByStatus(ReservationStatus.COMPLETED);

        assertTrue(response.isSuccess());
        assertEquals("Reservas por estado recuperadas correctamente.", response.getMessage());
        assertTrue(response.getData().isEmpty());
        verify(reservationRepository, times(1)).findByStatus(ReservationStatus.COMPLETED);
    }

    // --- Tests para getReservationById ---
    @Test
    void testGetReservationById_Success() {
        when(reservationRepository.findById(testReservation.getId())).thenReturn(Optional.of(testReservation));

        ApiResponse<ReservationResponse> response = reservationService.getReservationById(testReservation.getId());

        assertTrue(response.isSuccess());
        assertEquals("Reserva obtenida correctamente.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(testReservation.getId(), response.getData().getId());
        verify(reservationRepository, times(1)).findById(testReservation.getId());
    }

    @Test
    void testGetReservationById_NotFound() {
        Long nonExistentId = 99L;
        when(reservationRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        ApiResponse<ReservationResponse> response = reservationService.getReservationById(nonExistentId);

        assertFalse(response.isSuccess());
        assertEquals("Reserva con el id: " + nonExistentId + " no encontrada.", response.getMessage());
        assertNull(response.getData());
        verify(reservationRepository, times(1)).findById(nonExistentId);
    }

    // --- Tests para createReservation ---
    @Test
    void testCreateReservation_Success_NewClient() {
        ReservationCreateRequest request = ReservationCreateRequest.builder()
                .propertyId(testProperty.getId())
                .clientFirstName("Nuevo")
                .clientLastName("Cliente")
                .clientEmail("nuevo@example.com")
                .clientPhone("1198765432")
                .startDate(LocalDateTime.of(2025, 8, 1, 10, 0))
                .endDate(LocalDateTime.of(2025, 8, 5, 10, 0))
                .build();

        // Mockeo de dependencias
        when(propertyRepository.findById(request.getPropertyId())).thenReturn(Optional.of(testProperty));
        when(clientRepository.findByEmail(request.getClientEmail())).thenReturn(Optional.empty()); // Cliente nuevo
        when(clientRepository.save(any(Client.class))).thenReturn(testClient); // Guarda el nuevo cliente
        when(reservationRepository.findOverlappingReservations(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList()); // No hay solapamientos
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);
        when(contractRepository.save(any(Contract.class))).thenReturn(testContract);

        // Mockear ClassPathResource para evitar errores de archivo en tests unitarios
        try (MockedConstruction<ClassPathResource> mockedConstruction = mockConstruction(ClassPathResource.class,
                (mock, context) -> {
                    // 'mock' es la instancia recién creada y mockeada de ClassPathResource
                    when(mock.exists()).thenReturn(true);
                    // No necesitas stubbear getFile() o getInputStream() aquí si EmailService no los llama en este flujo
                    // Si los llama, tendrías que simular el archivo como en los otros tests.
                })) {

            // Mockear el envío de correo electrónico para que no cause errores reales
            doNothing().when(emailService).sendEmailWithAttachment(eq(testClient.getEmail()), anyString(), anyString(), any(Resource.class));

            ApiResponse<ReservationResponse> response = reservationService.createReservation(request);

            assertTrue(response.isSuccess());
            assertEquals("Reserva creada correctamente.", response.getMessage());
            assertNotNull(response.getData());
            assertEquals(testReservation.getId(), response.getData().getId());

            verify(propertyRepository, times(1)).findById(request.getPropertyId());
            verify(clientRepository, times(1)).findByEmail(request.getClientEmail());
            verify(clientRepository, times(1)).save(any(Client.class));
            verify(reservationRepository, times(1)).findOverlappingReservations(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
            verify(reservationRepository, times(2)).save(any(Reservation.class));
            verify(invoiceRepository, times(2)).save(any(Invoice.class));
            verify(contractRepository, times(2)).save(any(Contract.class));
            // Verifica que sendEmailWithAttachment fue llamado con cualquier Resource, ya que mockConstruction
            // devolverá un mock de ClassPathResource (que es un Resource)
            verify(emailService, times(2)).sendEmailWithAttachment(eq(testClient.getEmail()), anyString(), anyString(), any(Resource.class));
        } catch (Exception e) {
            fail("Exception occurred during test: " + e.getMessage());
        }
    }

    @Test
    void testCreateReservation_Success_ExistingClient() {
        ReservationCreateRequest request = ReservationCreateRequest.builder()
                .propertyId(testProperty.getId())
                .clientFirstName("Juan")
                .clientLastName("Perez")
                .clientEmail("juan.perez@example.com")
                .clientPhone("1123456789")
                .startDate(LocalDateTime.of(2025, 8, 1, 10, 0))
                .endDate(LocalDateTime.of(2025, 8, 5, 10, 0))
                .build();

        when(propertyRepository.findById(request.getPropertyId())).thenReturn(Optional.of(testProperty));
        when(clientRepository.findByEmail(request.getClientEmail())).thenReturn(Optional.of(testClient)); // Cliente existente
        when(reservationRepository.findOverlappingReservations(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList()); // No hay solapamientos

        // El primer save es para la reserva inicial, los siguientes para actualizar con invoice/contract
        when(reservationRepository.save(any(Reservation.class)))
                .thenReturn(testReservation) // Para el primer save de Reservation
         .thenAnswer(invocation -> { // Si necesitas modificar el objeto para el segundo save
             Reservation r = invocation.getArgument(0);
             r.setInvoice(testInvoice);
             r.setContract(testContract);
             return r;
         });

        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);
        when(contractRepository.save(any(Contract.class))).thenReturn(testContract);

        // Mockear ClassPathResource para evitar errores de archivo en tests unitarios
        try (MockedConstruction<ClassPathResource> mockedConstruction = mockConstruction(ClassPathResource.class,
                (mock, context) -> {
                    when(mock.exists()).thenReturn(true);
                    // Asegúrate de stubear getFile() o getInputStream() si emailService los llama
                    // Para un PDF simple:
                    try {
                        Path tempPath = Files.createTempFile("mock-pdf-", ".pdf");
                        Files.write(tempPath, "mock pdf content".getBytes());
                        File tempFile = tempPath.toFile();
                        tempFile.deleteOnExit();
                        when(mock.getFile()).thenReturn(tempFile);
                    } catch (IOException e) {
                        throw new RuntimeException("Error creando mock de archivo para Resource", e);
                    }
                })) {

            // Mockear el envío de correo electrónico para que no cause errores reales
            doNothing().when(emailService).sendEmailWithAttachment(anyString(), anyString(), anyString(), any(Resource.class));

            ApiResponse<ReservationResponse> response = reservationService.createReservation(request);

            assertTrue(response.isSuccess());
            assertEquals("Reserva creada correctamente.", response.getMessage());
            assertNotNull(response.getData());

            verify(propertyRepository, times(1)).findById(request.getPropertyId());
            verify(clientRepository, times(1)).findByEmail(request.getClientEmail());
            verify(clientRepository, never()).save(any(Client.class));
            verify(reservationRepository, times(1)).findOverlappingReservations(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class));
            verify(reservationRepository, times(2)).save(any(Reservation.class));
            verify(invoiceRepository, times(2)).save(any(Invoice.class));
            verify(contractRepository, times(2)).save(any(Contract.class));
            verify(emailService, times(2)).sendEmailWithAttachment(eq(testClient.getEmail()), anyString(), anyString(), any(Resource.class));
        } catch (Exception e) {
            fail("Exception occurred during test: " + e.getMessage());
        }
    }

    @Test
    void testUpdateReservationStatusByPayment_SuccessfullyConfirmsReservation() {
        // 1. Configurar el mock del repositorio para que devuelva la reserva cuando se busca
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(testReservation));

        // 2. Ejecutar el método que estamos probando (updateReservationStatusByPayment)
        reservationService.updateReservationStatusByPayment(1L, "approved");

        // 3. Verificar las interacciones y el estado final
        // Verificar que se buscó la reserva por ID
        verify(reservationRepository, times(1)).findById(1L);

        // Verificar que la reserva fue actualizada y guardada con el nuevo estado
        // Usa `argThat` o `any(Reservation.class)` si el objeto exacto puede cambiar
        verify(reservationRepository, times(1)).save(argThat(reservation ->
                reservation.getId().equals(1L) &&
                        reservation.getStatus().equals(ReservationStatus.CONFIRMED)
        ));

        // Opcional: Afirmar el estado del objeto testReservation si lo modificas directamente
        // assertEquals(ReservationStatus.CONFIRMED, testReservation.getStatus());
    }

    @Test
    void testUpdateReservationStatusByPayment_ReservationNotFound() {
        // 1. Configurar el mock para que no encuentre la reserva
        when(reservationRepository.findById(anyLong())).thenReturn(Optional.empty());

        // 2. Ejecutar el método
        // Puedes verificar que no lanza una excepción, o si debería lanzar una, la capturas
        // Aquí asumimos que no lanza excepción y simplemente no hace nada más
        reservationService.updateReservationStatusByPayment(99L, "APPROVED");

        // 3. Verificar que se intentó buscar pero no se guardó nada
        verify(reservationRepository, times(1)).findById(99L);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void testCreateReservation_PropertyNotFound() {
        ReservationCreateRequest request = ReservationCreateRequest.builder()
                .propertyId(99L) // ID que no existe
                .clientFirstName("Test")
                .clientLastName("User")
                .clientEmail("test@example.com")
                .clientPhone("1122334455")
                .startDate(LocalDateTime.of(2025, 8, 1, 10, 0))
                .endDate(LocalDateTime.of(2025, 8, 5, 10, 0))
                .build();

        when(propertyRepository.findById(request.getPropertyId())).thenReturn(Optional.empty());

        ApiResponse<ReservationResponse> response = reservationService.createReservation(request);

        assertFalse(response.isSuccess());
        assertEquals("Propiedad con el id: 99 no encontrada.", response.getMessage());
        assertNull(response.getData());
        verify(propertyRepository, times(1)).findById(request.getPropertyId());
        verify(reservationRepository, never()).save(any(Reservation.class)); // No se guarda nada
    }

    @Test
    void testCreateReservation_PropertyNotAvailable() {
        testProperty.setPropertyStatus(PropertyStatus.UNAVAILABLE); // Propiedad no disponible
        ReservationCreateRequest request = ReservationCreateRequest.builder()
                .propertyId(testProperty.getId())
                .clientFirstName("Test")
                .clientLastName("User")
                .clientEmail("test@example.com")
                .clientPhone("1122334455")
                .startDate(LocalDateTime.of(2025, 8, 1, 10, 0))
                .endDate(LocalDateTime.of(2025, 8, 5, 10, 0))
                .build();

        when(propertyRepository.findById(request.getPropertyId())).thenReturn(Optional.of(testProperty));

        ApiResponse<ReservationResponse> response = reservationService.createReservation(request);

        assertFalse(response.isSuccess());
        assertEquals("Propiedad no disponible para la reserva.", response.getMessage());
        assertNull(response.getData());
        verify(propertyRepository, times(1)).findById(request.getPropertyId());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void testCreateReservation_InvalidDates_StartDateAfterEndDate() {
        ReservationCreateRequest request = ReservationCreateRequest.builder()
                .propertyId(testProperty.getId())
                .clientFirstName("Test")
                .clientLastName("User")
                .clientEmail("test@example.com")
                .clientPhone("1122334455")
                .startDate(LocalDateTime.of(2025, 8, 5, 10, 0)) // Fecha de inicio posterior a fin
                .endDate(LocalDateTime.of(2025, 8, 1, 10, 0))
                .build();

        when(propertyRepository.findById(request.getPropertyId())).thenReturn(Optional.of(testProperty));

        ApiResponse<ReservationResponse> response = reservationService.createReservation(request);

        assertFalse(response.isSuccess());
        assertEquals("La fecha de inicio no puede ser posterior a la fecha de salida.", response.getMessage());
        assertNull(response.getData());
        verify(propertyRepository, times(1)).findById(request.getPropertyId());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void testCreateReservation_OverlappingReservations() {
        ReservationCreateRequest request = ReservationCreateRequest.builder()
                .propertyId(testProperty.getId())
                .clientFirstName("Test")
                .clientLastName("User")
                .clientEmail("test@example.com")
                .clientPhone("1122334455")
                .startDate(LocalDateTime.of(2025, 7, 12, 10, 0)) // Fechas que se solapan con testReservation
                .endDate(LocalDateTime.of(2025, 7, 14, 10, 0))
                .build();

        when(propertyRepository.findById(anyLong())).thenReturn(Optional.of(testProperty));


        // No necesitamos stubear save de invoice/contract/reservation aquí, ya que el flujo se detiene antes

        // IMPORTANTÍSIMO: Mockear findOverlappingReservations para que devuelva una reserva existente
        when(reservationRepository.findOverlappingReservations(
                request.getPropertyId(),
                request.getStartDate(),
                request.getEndDate()
        )).thenReturn(Collections.singletonList(testReservation)); // Devuelve una lista con una reserva para simular solapamiento

        // Mockear el envío de correo electrónico para que no cause errores reales (no debería llamarse en este test)

        // 3. Mockear el comportamiento estático de ClassPathResource
        // Aunque el email no debería enviarse en este caso, es buena práctica tener el mockStatic
        // para que no cause problemas si el flujo del servicio cambia o si hay caminos inesperados.
        try (MockedConstruction<ClassPathResource> mockedConstruction = mockConstruction(ClassPathResource.class,
                (mock, context) -> {
                    when(mock.exists()).thenReturn(true);
                    // En este test, el emailService no debería llamarse, así que la lógica de getFile() no es estrictamente necesaria aquí
                    // a menos que tu servicio la llame por alguna razón antes de decidir no enviar el email.
                })) {

            // Mockear el envío de correo electrónico para que no cause errores reales (y verificar que no se llama)

            ApiResponse<ReservationResponse> response = reservationService.createReservation(request);

            assertNotNull(response, "La respuesta no debería ser nula.");
            assertFalse(response.isSuccess(), "La operación debería fallar debido a reservas superpuestas.");
            assertEquals("La propiedad ya esta reservada para las fechas seleccionadas.", response.getMessage(), "El mensaje de error no coincide.");
            assertNull(response.getData(), "Los datos de la respuesta deberían ser nulos en caso de fallo.");

            verify(propertyRepository, times(1)).findById(request.getPropertyId());
            verify(clientRepository, never()).findByEmail(request.getClientEmail());
            verify(reservationRepository, times(1)).findOverlappingReservations(
                    request.getPropertyId(),
                    request.getStartDate(),
                    request.getEndDate()
            );
            verify(reservationRepository, never()).save(any(Reservation.class));
            verify(invoiceRepository, never()).save(any(Invoice.class));
            verify(contractRepository, never()).save(any(Contract.class));
            verify(emailService, never()).sendEmailWithAttachment(anyString(), anyString(), anyString(), any(Resource.class));
        }  catch (Exception e) {
            fail("Ocurrió una excepción inesperada durante el test: " + e.getMessage());
        }
    }

    @Test
    void testCreateReservation_EmailSendFails() {
        ReservationCreateRequest request = ReservationCreateRequest.builder()
                .propertyId(testProperty.getId())
                .clientFirstName("Nuevo")
                .clientLastName("Cliente")
                .clientEmail("nuevo@example.com")
                .clientPhone("1198765432")
                .startDate(LocalDateTime.of(2025, 8, 1, 10, 0))
                .endDate(LocalDateTime.of(2025, 8, 5, 10, 0))
                .build();

        when(propertyRepository.findById(request.getPropertyId())).thenReturn(Optional.of(testProperty));
        when(clientRepository.findByEmail(request.getClientEmail())).thenReturn(Optional.empty()); // Cliente nuevo
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);
        when(reservationRepository.findOverlappingReservations(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(reservationRepository.save(any(Reservation.class)))
                .thenReturn(testReservation); // Primer save de Reservation
        // .thenAnswer(invocation -> { // Si necesitas modificar el objeto para el segundo save
        //     Reservation r = invocation.getArgument(0);
        //     r.setInvoice(testInvoice);
        //     r.setContract(testContract);
        //     return r;
        // });
        when(invoiceRepository.save(any(Invoice.class))).thenReturn(testInvoice);
        when(contractRepository.save(any(Contract.class))).thenReturn(testContract);

        // Simular que el envío de email falla
        try (MockedConstruction<ClassPathResource> mockedConstruction = mockConstruction(ClassPathResource.class,
                (mock, context) -> {
                    when(mock.exists()).thenReturn(true);
                    // Stubbear getFile() o getInputStream() si emailService los llama.
                    // La lógica de archivo temporal es buena aquí si el emailService intenta leer el recurso.
                    try {
                        Path tempPath = Files.createTempFile("mock-pdf-", ".pdf");
                        Files.write(tempPath, "mock pdf content".getBytes());
                        File tempFile = tempPath.toFile();
                        tempFile.deleteOnExit();
                        when(mock.getFile()).thenReturn(tempFile);
                    } catch (IOException e) {
                        throw new RuntimeException("Error creando mock de archivo para Resource", e);
                    }
                })) {

            // Simular que el envío de email lanza una excepción
            doThrow(new RuntimeException("Error de envío de mail simulado"))
                    .when(emailService).sendEmailWithAttachment(eq(testClient.getEmail()), anyString(), anyString(), any(Resource.class));

            ApiResponse<ReservationResponse> response = reservationService.createReservation(request);

            assertTrue(response.isSuccess());
            assertEquals("Reserva creada correctamente.", response.getMessage());
            assertNotNull(response.getData());

            verify(emailService, times(1)).sendEmailWithAttachment(eq(testClient.getEmail()), anyString(), anyString(), any(Resource.class));
            verify(invoiceRepository, times(1)).save(any(Invoice.class));
            verify(contractRepository, times(1)).save(any(Contract.class));

            assertEquals(DocumentStatus.PENDING, testInvoice.getStatus());
            assertEquals(DocumentStatus.PENDING, testContract.getStatus());

        } catch (Exception e) {
            fail("Exception occurred during test setup: " + e.getMessage());
        }

    }


    // --- Tests para updateReservationStatusByPayment ---
    @Test
    void testUpdateReservationStatusByPayment_Approved() {
        when(reservationRepository.findById(testReservation.getId())).thenReturn(Optional.of(testReservation));

        reservationService.updateReservationStatusByPayment(testReservation.getId(), "approved");

        assertEquals(ReservationStatus.CONFIRMED, testReservation.getStatus());
        verify(reservationRepository, times(1)).save(testReservation);
    }

    @Test
    void testUpdateReservationStatusByPayment_Pending() {
        testReservation.setStatus(ReservationStatus.CONFIRMED); // Cambiamos el estado para probar el "pending"
        when(reservationRepository.findById(testReservation.getId())).thenReturn(Optional.of(testReservation));

        reservationService.updateReservationStatusByPayment(testReservation.getId(), "pending");

        assertEquals(ReservationStatus.PENDING, testReservation.getStatus());
        verify(reservationRepository, times(1)).save(testReservation);
    }

    @Test
    void testUpdateReservationStatusByPayment_Rejected() {
        when(reservationRepository.findById(testReservation.getId())).thenReturn(Optional.of(testReservation));

        reservationService.updateReservationStatusByPayment(testReservation.getId(), "rejected");

        assertEquals(ReservationStatus.CANCELLED, testReservation.getStatus());
        verify(reservationRepository, times(1)).save(testReservation);
    }


    @Test
    void testUpdateReservationStatusByPayment_SameStatus() {
        testReservation.setStatus(ReservationStatus.PENDING);
        when(reservationRepository.findById(testReservation.getId())).thenReturn(Optional.of(testReservation));

        reservationService.updateReservationStatusByPayment(testReservation.getId(), "pending"); // Estado ya es PENDING

        assertEquals(ReservationStatus.PENDING, testReservation.getStatus());
        verify(reservationRepository, never()).save(any(Reservation.class)); // No se guarda si el estado es el mismo
    }


    @Test
    void testConfirmReservation_NotFound() {
        Long nonExistentId = 99L;
        when(reservationRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        ApiResponse<ReservationResponse> response = reservationService.confirmReservation(nonExistentId);

        assertFalse(response.isSuccess());
        assertEquals("Reserva con el id: " + nonExistentId + " no encontrada.", response.getMessage());
        verify(reservationRepository, times(1)).findById(nonExistentId);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void testConfirmReservation_NotPending() {
        testReservation.setStatus(ReservationStatus.COMPLETED); // Estado no pendiente
        when(reservationRepository.findById(testReservation.getId())).thenReturn(Optional.of(testReservation));

        ApiResponse<ReservationResponse> response = reservationService.confirmReservation(testReservation.getId());

        assertFalse(response.isSuccess());
        assertEquals("Solo las reservas pendientes pueden ser confirmadas.", response.getMessage());
        verify(reservationRepository, times(1)).findById(testReservation.getId());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    // --- Tests para cancelReservation ---
    @Test
    void testCancelReservation_Success() {
        testReservation.setStatus(ReservationStatus.PENDING); // Puede ser pendiente o confirmada
        when(reservationRepository.findById(testReservation.getId())).thenReturn(Optional.of(testReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        ApiResponse<ReservationResponse> response = reservationService.cancelReservation(testReservation.getId());

        assertTrue(response.isSuccess());
        assertEquals("Reserva cancelada correctamente.", response.getMessage());
        assertEquals(ReservationStatus.CANCELLED, testReservation.getStatus());
        verify(reservationRepository, times(1)).findById(testReservation.getId());
        verify(reservationRepository, times(1)).save(testReservation);
    }

    @Test
    void testCancelReservation_NotFound() {
        Long nonExistentId = 99L;
        when(reservationRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        ApiResponse<ReservationResponse> response = reservationService.cancelReservation(nonExistentId);

        assertFalse(response.isSuccess());
        assertEquals("Reserva con el id: " + nonExistentId + " no encontrada.", response.getMessage());
        verify(reservationRepository, times(1)).findById(nonExistentId);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void testCancelReservation_AlreadyCompleted() {
        testReservation.setStatus(ReservationStatus.COMPLETED);
        when(reservationRepository.findById(testReservation.getId())).thenReturn(Optional.of(testReservation));

        ApiResponse<ReservationResponse> response = reservationService.cancelReservation(testReservation.getId());

        assertFalse(response.isSuccess());
        assertEquals("No se puede cancelar una reserva ya completada o cancelada.", response.getMessage());
        verify(reservationRepository, times(1)).findById(testReservation.getId());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void testCancelReservation_AlreadyCancelled() {
        testReservation.setStatus(ReservationStatus.CANCELLED);
        when(reservationRepository.findById(testReservation.getId())).thenReturn(Optional.of(testReservation));

        ApiResponse<ReservationResponse> response = reservationService.cancelReservation(testReservation.getId());

        assertFalse(response.isSuccess());
        assertEquals("No se puede cancelar una reserva ya completada o cancelada.", response.getMessage());
        verify(reservationRepository, times(1)).findById(testReservation.getId());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    // --- Tests para completeReservation ---
    @Test
    void testCompleteReservation_Success() {
        testReservation.setStatus(ReservationStatus.CONFIRMED); // Asegurar que está confirmada
        when(reservationRepository.findById(testReservation.getId())).thenReturn(Optional.of(testReservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(testReservation);

        ApiResponse<ReservationResponse> response = reservationService.completeReservation(testReservation.getId());

        assertTrue(response.isSuccess());
        assertEquals("Reserva completada.", response.getMessage());
        assertEquals(ReservationStatus.COMPLETED, testReservation.getStatus());
        verify(reservationRepository, times(1)).findById(testReservation.getId());
        verify(reservationRepository, times(1)).save(testReservation);
    }

    @Test
    void testCompleteReservation_NotFound() {
        Long nonExistentId = 99L;
        when(reservationRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        ApiResponse<ReservationResponse> response = reservationService.completeReservation(nonExistentId);

        assertFalse(response.isSuccess());
        assertEquals("Reserva con el id: " + nonExistentId + " no encontrada.", response.getMessage());
        verify(reservationRepository, times(1)).findById(nonExistentId);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void testCompleteReservation_NotConfirmed() {
        testReservation.setStatus(ReservationStatus.PENDING); // Estado no confirmada
        when(reservationRepository.findById(testReservation.getId())).thenReturn(Optional.of(testReservation));

        ApiResponse<ReservationResponse> response = reservationService.completeReservation(testReservation.getId());

        assertFalse(response.isSuccess());
        assertEquals("Solo las reservas confirmadas pueden ser completadas.", response.getMessage());
        verify(reservationRepository, times(1)).findById(testReservation.getId());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }
}
