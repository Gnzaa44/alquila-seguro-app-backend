package com.example.alquila_seguro_backend.entity;

/**
 * Constantes con los estados de los documentos,
 * (Factura y contrato de caución).
 */
public enum DocumentStatus {
    /**
     * Documento generado, pero no enviado.
     */
    PENDING,

    /**
     * Documento enviado por correo electrónico.
     */
    SENT,

    /**
     *  Error al enviar el documento
     */
    ERROR
}
