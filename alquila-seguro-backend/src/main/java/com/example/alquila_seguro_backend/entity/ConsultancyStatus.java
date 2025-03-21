package com.example.alquila_seguro_backend.entity;

/**
 * Constantes con los estados de una consultoria.
 */
public enum ConsultancyStatus {
    /**
     * Consultoria a la espera de ser respondida por el veedor.
     */
    PENDING,

    /**
     * Consultoria respondida por el veedor.
     */
    RESPONDED,

    /**
     * Consultoria ya cerrada entre el cliente y el veedor.
     */
    CLOSED
}
