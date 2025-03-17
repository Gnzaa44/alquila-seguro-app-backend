package com.example.alquila_seguro_backend.services;

import com.example.alquila_seguro_backend.dto.ApiResponse;
import com.example.alquila_seguro_backend.dto.ClientCreateRequest;
import com.example.alquila_seguro_backend.dto.ClientResponse;
import com.example.alquila_seguro_backend.entity.Client;
import com.example.alquila_seguro_backend.repositories.ClientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
    public ApiResponse<List<ClientResponse>> getAllClients() {
        List<ClientResponse> clients = clientRepository.findAll()
                .stream()
                .map(this::mapToClientResponse)
                .collect(Collectors.toList());
        return ApiResponse.<List<ClientResponse>>builder()
                .success(true)
                .message("Clientes recuperados con exito")
                .data(clients)
                .build();

    }
    public ApiResponse<ClientResponse> getClientById(Long id) {
        return clientRepository.findById(id)
                .map(client -> ApiResponse.<ClientResponse>builder()
                        .success(true)
                        .message("Cliente recuperado con exito")
                        .data(mapToClientResponse(client))
                        .build())
                .orElse(ApiResponse.<ClientResponse>builder()
                .success(false).message("Cliente con id : " + id + " no encontrado").build());

    }

    @Transactional
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
    @Transactional
    public ApiResponse<ClientResponse> updateClient(Long id, ClientCreateRequest request) {
        return clientRepository.findById(id)
                .map(client -> {
                    // Check if email is being changed and if it's already in use
                    if (!client.getEmail().equals(request.getEmail()) &&
                            clientRepository.existsByEmail(request.getEmail())) {
                        return ApiResponse.<ClientResponse>builder()
                                .success(false)
                                .message("El Email ya existe")
                                .build();
                    }

                    client.setFirstName(request.getFirstName());
                    client.setLastName(request.getLastName());
                    client.setEmail(request.getEmail());
                    client.setPhone(request.getPhone());

                    Client updatedClient = clientRepository.save(client);
                    return ApiResponse.<ClientResponse>builder()
                            .success(true)
                            .message("Cliente actualizado correctamente")
                            .data(mapToClientResponse(updatedClient))
                            .build();
                })
                .orElse(ApiResponse.<ClientResponse>builder()
                        .success(false)
                        .message("Cliente con el id: " + id + " no encontrado")
                        .build());
    }
    @Transactional
    public ApiResponse<Void> deleteClient(Long id) {
        return clientRepository.findById(id)
                .map(client -> {
                    clientRepository.delete(client);
                    return ApiResponse.<Void>builder()
                            .success(true)
                            .message("Cliente eliminado correctamente")
                            .build();
                })
                .orElse(ApiResponse.<Void>builder()
                        .success(false)
                        .message("Cliente con id: " + id + " no encontrado")
                        .build());
    }




}
