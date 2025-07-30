package com.example.alquila_seguro_backend.mercadopago.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.codec.digest.HmacUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Component
public class SignatureHelper {
    private static final Logger logger = LoggerFactory.getLogger(SignatureHelper.class);

    @Value("${mercadopago.secret-key}")
    private final String secretKey;

    public SignatureHelper(@Value("${mercadopago.secret-key}") String secretKey) {
        this.secretKey = secretKey;
    }
    /**
     * Genera la firma HMAC-SHA256 para validar notificaciones de Mercado Pago (topic 'payment').
     *
     * @param xRequestIdHeader         La HttpServletRequest recibida de Mercado Pago.
     * @param tsHeader La cabecera 'x-signature' recibida.
     * @return True si la firma es válida, false de lo contrario.
     */
    public boolean isValidPaymentNotificationSignature(String dataIdFromQueryParam, String xRequestIdHeader, String tsHeader, String v1SignatureReceived) {
        // Estas validaciones iniciales son para asegurar que los valores NECESARIOS (dataIdFromQueryParam, ts, v1) estén presentes.
        // xRequestIdHeader puede ser opcional dependiendo de la notificación.
        if (dataIdFromQueryParam == null || dataIdFromQueryParam.isEmpty() || // Ahora validamos dataIdFromQueryParam
                tsHeader == null || tsHeader.isEmpty() ||
                v1SignatureReceived == null || v1SignatureReceived.isEmpty()) {
            logger.warn("Parámetros clave (data.id, ts, v1) o cabeceras faltantes para la validación de firma: data.id='{}', ts='{}', v1='{}'.", dataIdFromQueryParam, tsHeader, v1SignatureReceived);
            return false;
        }

        // *** CAMBIO CLAVE AQUÍ: Construcción condicional del stringToSign (manifest) ***
        StringBuilder stringToSignBuilder = new StringBuilder();

        // data.id_url (query param) es SIEMPRE requerido para el tipo 'payment' en la firma v1
        stringToSignBuilder.append("id:").append(dataIdFromQueryParam).append(";");

        // request-id (x-request-id header) es opcional, solo se añade si está presente
        if (xRequestIdHeader != null && !xRequestIdHeader.isEmpty()) {
            stringToSignBuilder.append("request-id:").append(xRequestIdHeader).append(";");
        }

        // ts (timestamp de x-signature) es SIEMPRE requerido
        stringToSignBuilder.append("ts:").append(tsHeader).append(";");

        String stringToSign = stringToSignBuilder.toString();


        logger.debug("Cadena a firmar (construida): '{}'", stringToSign);
        logger.debug("Firma recibida (v1): {}", v1SignatureReceived);


        try {
            // Asegúrate de que tu secretKey se está usando como bytes y la cadena a firmar también.
            // Si usas HmacUtils de Apache Commons Codec, es más directo:
            // String calculatedSignature = HmacUtils.hmacSha256Hex(secretKey.getBytes(StandardCharsets.UTF_8), stringToSign.getBytes(StandardCharsets.UTF_8));

            // Implementación manual de HMAC-SHA256 si no usas HmacUtils:
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmacSha256.init(secretKeySpec);
            byte[] hmacBytes = hmacSha256.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            String calculatedSignature = bytesToHex(hmacBytes); // Convertir bytes a String hexadecimal

            logger.debug("Firma calculada: {}", calculatedSignature);

            if (calculatedSignature.equals(v1SignatureReceived)) {
                logger.info("Firma de webhook válida.");
                return true;
            } else {
                logger.warn("Firma de webhook inválida. Calculada: {}, Recibida: {}. Revise secret-key y construcción de la cadena.", calculatedSignature, v1SignatureReceived);
                return false;
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            logger.error("Error de configuración de HMAC-SHA256: {}", e.getMessage(), e);
            return false;
        } catch (Exception e) {
            logger.error("Error al calcular la firma HMAC: {}", e.getMessage(), e);
            return false;
        }
    }
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
