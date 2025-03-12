package com.example.alquila_seguro_backend.entity;

public enum DocumentStatus {
    PENDING, // Documento generado, pero no enviado
    SENT,    // Documento enviado por correo electrónico
    ERROR    // Error al enviar el documento
}
