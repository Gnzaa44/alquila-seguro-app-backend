package com.example.alquila_seguro_backend.entity;

/**
 * Constantes con los estados de una propiedad.
 */
public enum PropertyStatus {
    /**
     * Propiedad disponible para la reserva.
     * Utilizado para propiedades de reserva inmediata.
     */
    AVAILABLE,

    /**
     * Propiedad no disponible para la reserva.
     */
    UNAVAILABLE,

    /**
     * Se necesita una revision antes de poder estar disponible.
     * Utilizada para propiedades de consultoria requerida.
     */
    NEEDS_REVIEW,

    /**
     * Verificacion completada y lista para ser reservada.
     * Utilizada para propiedades de consultoria requerida, post-revision.
     */
    VERIFIED_AVAILABLE,

    /**
     * Propiedad reservada.
     */
    RESERVED

}
