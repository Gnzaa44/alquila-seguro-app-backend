package com.example.alquila_seguro_backend.services;

import com.example.alquila_seguro_backend.dto.ApiResponse;
import com.example.alquila_seguro_backend.dto.ClientCreateRequest;
import com.example.alquila_seguro_backend.dto.ClientResponse;
import com.example.alquila_seguro_backend.entity.Client;
import com.example.alquila_seguro_backend.repositories.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;

    // Helper method to map Client entity to ClientResponse DTO
    private ClientResponse mapToClientResponse(Client client) {
        return ClientResponse.builder()
                .id(client.getId())
                .firstName(client.getFirstName())
                .lastName(client.getLastName())
                .email(client.getEmail())
                .phone(client.getPhone())
                .createdAt(client.getCreatedAt())
                .build();
    }

    public ApiResponse<ClientResponse>createClient(ClientCreateRequest clientCreateRequest) {
        if(clientRepository.existsByEmail(clientCreateRequest.getEmail())) {
            return ApiResponse.<ClientResponse>builder()
                    .success(false)
                    .message("Mail ya en uso")
                    .build();
        }

        Client client =  Client.builder()
                .firstName(clientCreateRequest.getFirstName())
                .lastName(clientCreateRequest.getLastName())
                .email(clientCreateRequest.getEmail())
                .phone(clientCreateRequest.getPhone())
                .build();

        Client savedClient=clientRepository.save(client);
        return ApiResponse.<ClientResponse>builder()
                .success(true)
                .message("Cliente registrado correctamente")
                .data(mapToClientResponse(savedClient))
                .build();

    }

}
