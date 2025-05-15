package com.example.alquila_seguro_backend.mercadopago.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.codec.digest.HmacUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class SignatureHelper {
    private static final Logger logger = LoggerFactory.getLogger(SignatureHelper.class);

    private final String secretKey;

    public SignatureHelper(@Value("${mercadopago.secret-key}") String secretKey) {
        this.secretKey = secretKey;
    }
    /**
     * Genera la firma HMAC-SHA256 para validar notificaciones de Mercado Pago (topic 'payment').
     *
     * @param request         La HttpServletRequest recibida de Mercado Pago.
     * @param signatureHeader La cabecera 'x-signature' recibida.
     * @return True si la firma es válida, false de lo contrario.
     */
    public boolean isValidPaymentNotificationSignature(HttpServletRequest request, String signatureHeader) {
        String ts = null;
        String v1 = null;
        if (signatureHeader != null && !signatureHeader.isEmpty()) {
            String[] parts = signatureHeader.split(",");
            for (String part : parts) {
                String[] keyValue = part.split("=");
                logger.debug("Parte del header: {}, Key: {}, Value: {}", part, keyValue[0], keyValue[1]);
                if (keyValue.length == 2) {
                    if ("ts".equals(keyValue[0])) {
                        ts = keyValue[1];
                    } else if ("v1".equals(keyValue[0])) {
                        v1 = keyValue[1];
                    }
                }
            }

        }

        String dataIdUrl = request.getParameter("data.id");
        String xRequestIdHeader = request.getHeader("x-request-id");

        if (dataIdUrl == null || xRequestIdHeader == null || ts == null) {
            logger.warn("Parámetros o cabeceras faltantes para la validación (data.id={}, x-request-id={}, ts={}).", dataIdUrl, xRequestIdHeader, ts);
            return false;
        }

        // Construir la cadena para la firma EXACTAMENTE como lo indica la documentación
        String stringToSign = String.format("id:%s;request-id:%s;ts:%s;", dataIdUrl, xRequestIdHeader, ts);
        logger.debug("Cadena a firmar (construida): {}", stringToSign);
        logger.debug("Clave secreta utilizada para la validación: {}", this.secretKey);

        try {
            String calculatedSignature = HmacUtils.hmacSha256Hex(secretKey.getBytes(StandardCharsets.UTF_8), stringToSign.getBytes(StandardCharsets.UTF_8));
            logger.debug("Firma calculada: {}", calculatedSignature);
            logger.debug("Firma recibida (v1): {}", v1);
            return calculatedSignature.equals(v1);
        } catch (Exception e) {
            logger.error("Error al calcular la firma HMAC: {}", e.getMessage(), e);
            return false;
        }
    }
}
