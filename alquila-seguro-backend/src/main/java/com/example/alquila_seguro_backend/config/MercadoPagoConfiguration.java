package com.example.alquila_seguro_backend.config;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceClient;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MercadoPagoConfiguration {

    @Value("${MERCADO_PAGO_ACCESS_TOKEN}")
    private String accessToken;

    @PostConstruct
    public void initialize() {
        MercadoPagoConfig.setAccessToken(accessToken);
    }

    @Bean
    public PreferenceClient preferenceClient() {
        MercadoPagoConfig.setAccessToken(accessToken); // Configura el access token globalmente
        return new PreferenceClient();
    }

    @Bean
    public PaymentClient paymentClient() {
        MercadoPagoConfig.setAccessToken(accessToken); // Asegúrate de que el access token esté configurado
        return new PaymentClient();
    }
}

