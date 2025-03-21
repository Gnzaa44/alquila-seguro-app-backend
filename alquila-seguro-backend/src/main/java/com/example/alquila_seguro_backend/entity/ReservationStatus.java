package com.example.alquila_seguro_backend.entity;

/**
 *  Constantes con los distintos estados de una reserva.
 */
public enum ReservationStatus {
    /**
     *  La reserva ha sido creada, pero aún no ha sido confirmada.
     * Puede significar que el cliente ha solicitado la reserva,
     * pero aún no ha realizado el pago o que la confirmación está pendiente de revisión por parte del administrador.
     */
    PENDING,

    /**
     * Este estado indica que la reserva ha sido confirmada con éxito.
     * El cliente ha realizado el pago o la reserva ha sido aprobada por el administrador.
     * La propiedad está ahora reservada para el cliente durante el período acordado.
     */
    CONFIRMED,

    /**
     * Este estado indica que la reserva ha sido cancelada.
     * La cancelación puede haber sido realizada por el cliente o por el administrador.
     * La propiedad vuelve a estar disponible para otras reservas.
     */
    CANCELLED,
    COMPLETED
}
