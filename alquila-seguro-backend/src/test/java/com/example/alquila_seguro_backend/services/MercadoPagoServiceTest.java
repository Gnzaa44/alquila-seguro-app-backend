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
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doAnswer;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class MercadoPagoServiceTest {
    @Mock
    private ReservationService reservationService;

    @Mock
    private ReservationRepository reservationRepository;
    @InjectMocks
    private MercadoPagoService mercadoPagoService;
    @Mock
    private PaymentRepository paymentRepository;


    private Reservation testReservation;
    private Client testClient;
    private Property testProperty;
    private Invoice testInvoice;
    private PaymentResponse testPaymentResponse;
    private Contract testContract;
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
                .build();
        testReservation = Reservation.builder()
                .id(1L)
                .property(testProperty)
                .client(testClient)
                .startDate(LocalDateTime.of(2025, 8, 1, 10, 0))
                .endDate(LocalDateTime.of(2025, 8, 5, 10, 0))
                .status(ReservationStatus.PENDING) // Importante: empieza PENDING
                .build();
        testInvoice = Invoice.builder().id(1L).status(DocumentStatus.PENDING).build();
        testContract = Contract.builder().id(1L).status(DocumentStatus.PENDING).build();

        // Inicializa el mock del payment para la reserva
        testPayment = com.example.alquila_seguro_backend.entity.Payment.builder()
                .id(1L)
                .reservation(testReservation)
                .amount(new BigDecimal("1000.00"))
                .paymentStatus(PaymentStatus.PENDING) // Inicialmente PENDING
                .paymentReference("MP_PREF_123")
                .paymentIdMP(null) // Todavía no tenemos un ID de MP real
                .externalId(String.valueOf(testReservation.getId()))
                .externalEntityType(ExternalEntityType.RESERVATION)
                .build();


        // Inicializar el PaymentResponse como si viniera de Mercado Pago
        testPaymentResponse = new PaymentResponse();
        testPaymentResponse.setId(1L); // ID del pago de MP
        testPaymentResponse.setPaymentStatus(PaymentStatus.APPROVED); // Estado "approved" simulado
        testPaymentResponse.setReservationId(testReservation.getId());// ID de reserva para el flujo



        // Configuración para el escenario de "primera notificación" (paymentIdMP es null en tu BD)
        // 1. Mockear paymentRepository.findByPaymentIdMP(String.valueOf(paymentMP.getId()))
        // En la primera notificación, no debería encontrar el pago por su ID de MP.
        when(paymentRepository.findByPaymentIdMP(String.valueOf(testPaymentResponse.getId())))
                .thenReturn(Optional.empty()); // No encuentra el pago por ID de MP

        // 2. Mockear paymentRepository.findByReservationIdAndPaymentStatus(...)
        // Si no lo encuentra por paymentIdMP, tu servicio busca por reservationId y PENDING.
        // Aquí debe encontrar nuestro testPayment PENDING.
        when(paymentRepository.findByReservationIdAndPaymentStatus(testReservation.getId(), PaymentStatus.PENDING))
                .thenReturn(Optional.of(testPayment)); // Encuentra el pago PENDING para la reserva

        // 3. Mockear paymentRepository.save(Payment)
        // Cuando se llama a save con CUALQUIER Payment, devuelve el mismo objeto Payment que le pasaron.
        // Esto es crucial para que el objeto 'testPayment' se "actualice" con el nuevo estado
        // y el 'paymentIdMP' real dentro de tu test.
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment savedPayment = invocation.getArgument(0);
            // Simular el cambio de estado y el setting del paymentIdMP
            // El `processPaymentUpdate` lo hace internamente y luego llama a save
            // Por lo tanto, no necesitamos simularlo aquí directamente, Mockito lo captura
            return savedPayment; // Devuelve el mismo objeto Payment que se le pasó
        });

        // 4. Mockear el comportamiento de reservationService.updateReservationStatusByPayment
        // Este es el mock que simula lo que hace tu ReservationService.
        // No llamamos al método real de ReservationService, solo simulamos su efecto.
        doAnswer(invocation -> {
            Long reservationId = invocation.getArgument(0);
            String paymentStatusString = invocation.getArgument(1);


            // Simular el cambio de estado en la reserva, tal como lo haría el ReservationService real
            if ("approved".equalsIgnoreCase(paymentStatusString)) {
                testReservation.setStatus(ReservationStatus.CONFIRMED);
            } else if ("pending".equalsIgnoreCase(paymentStatusString)) {
                testReservation.setStatus(ReservationStatus.PENDING);
            } else { // rejected, cancelled, refunded
                testReservation.setStatus(ReservationStatus.CANCELLED);
            }
            return null; // El método updateReservationStatusByPayment es void
        }).when(reservationService).updateReservationStatusByPayment(anyLong(), anyString());

    }



}

