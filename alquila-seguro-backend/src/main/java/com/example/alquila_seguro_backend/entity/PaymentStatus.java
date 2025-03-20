package com.example.alquila_seguro_backend.entity;

/**
 * Constantes para manejar estados, procesos y resultados de transacciones.
 */
public enum PaymentStatus {
    /**
     * Estado pendiente de procesamiento.
     * Pudo ser iniciado por el usuario,
     * pero aún no se completa la autorización o captura.
     */
    PENDING,

    /**
     * El pago ha sido autorizado y capturado exitosamente.
     */
    APPROVED,

    /**
     * Pago autorizado pero no capturado.
     * Común en pagos con tarjeta de crédito,
     * donde se reserva el monto y luego se captura.
     */
    AUTHORIZED,

    /**
     * Pago en proceso de analisis.
     */
    IN_PROGRESS,

    /**
     * Pago cancelado.
     */
    CANCELLED,

    /**
     * Pago reembolsado al usuario.
     */
    REFUNDED,

    /**
     * Se ha iniciado un reclamo.
     */
    IN_MEDIATION,

    /**
     * Pago rechazado por el proveedor de pagos o por Mercado Pago.
     * Ocurre debido a error o restricción.
     */
    REJECTED,

    /**
     * Se produce una devolución de cargo (Contracargo).
     */
    RECHARGED_BACK
}
