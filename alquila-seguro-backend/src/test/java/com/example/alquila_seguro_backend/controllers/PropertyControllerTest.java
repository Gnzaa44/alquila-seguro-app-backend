package com.example.alquila_seguro_backend.controllers;
import com.example.alquila_seguro_backend.controller.PropertyController;
import com.example.alquila_seguro_backend.dto.ApiResponse;
import com.example.alquila_seguro_backend.dto.PropertyCreateRequest;
import com.example.alquila_seguro_backend.dto.PropertyResponse;
import com.example.alquila_seguro_backend.entity.PropertyStatus;
import com.example.alquila_seguro_backend.security.config.SecurityConfig;
import com.example.alquila_seguro_backend.security.jwt.JwtAuthEntryPoint;
import com.example.alquila_seguro_backend.security.jwt.JwtAuthFilter;
import com.example.alquila_seguro_backend.security.service.UserDetailsImpl;
import com.example.alquila_seguro_backend.services.PropertyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.example.alquila_seguro_backend.security.service.UserDetailsServiceImpl;


import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import com.example.alquila_seguro_backend.security.jwt.JwtUtils; // << IMPORTAR JwtUtils


import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PropertyController.class)
@Import({SecurityConfig.class, JwtAuthEntryPoint.class, JwtAuthFilter.class, JwtUtils.class}) // Importa tu configuración de seguridad para que se aplique
class PropertyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private PropertyService propertyService;

    @Autowired
    private ObjectMapper objectMapper; // Para convertir objetos a JSON

    private PropertyResponse createMockPropertyResponse(Long id, String title, PropertyStatus status) {
        return PropertyResponse.builder()
                .id(id)
                .title(title)
                .description("Desc")
                .location("Loc")
                .pricePerNight(100.0)
                .category("Cat")
                .latitude(new BigDecimal("10.0"))
                .longitude(new BigDecimal("20.0"))
                .numberOfRooms(1)
                .numberOfBathrooms(1)
                .size(new BigDecimal("50.0"))
                .features(new HashSet<>(Collections.singletonList("Feat")))
                .amenities(new HashSet<>(Collections.singletonList("Amen")))
                .imageUrl("http://img.url")
                .propertyStatus(status)
                .build();
    }

    private PropertyCreateRequest createMockPropertyCreateRequest(String title, PropertyStatus status) {
        return PropertyCreateRequest.builder()
                .title(title)
                .description("Desc de la propiedad")
                .location("Location Test")
                .pricePerNight(100.0)
                .category("Category Test")
                .latitude(new BigDecimal("10.0"))
                .longitude(new BigDecimal("20.0"))
                .numberOfRooms(2)
                .numberOfBathrooms(1)
                .size(new BigDecimal("70.0"))
                .features(new HashSet<>(Collections.singletonList("Feature A")))
                .amenities(new HashSet<>(Collections.singletonList("Amenity B")))
                .imageUrl("http://image.com/test.jpg")
                .propertyStatus(status)
                .build();
    }

    // --- GET Endpoints (Públicos) ---

    @Test
    @DisplayName("Debe obtener todas las propiedades exitosamente")
    void getAllProperties_shouldReturnOk() throws Exception {
        // Dado
        List<PropertyResponse> mockProperties = Arrays.asList(
                createMockPropertyResponse(1L, "Prop1", PropertyStatus.AVAILABLE),
                createMockPropertyResponse(2L, "Prop2", PropertyStatus.RESERVED)
        );
        when(propertyService.getAllProperties()).thenReturn(ApiResponse.<List<PropertyResponse>>builder()
                .success(true).message("OK").data(mockProperties).build());

        // Cuando y Entonces
        mockMvc.perform(get("/alquila-seg/properties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("Prop1"));
        verify(propertyService, times(1)).getAllProperties();
    }

    @Test
    @DisplayName("Debe obtener propiedades disponibles exitosamente")
    void getAvailableProperties_shouldReturnOk() throws Exception {
        // Dado
        List<PropertyResponse> mockProperties = Collections.singletonList(
                createMockPropertyResponse(1L, "Prop1 Disponible", PropertyStatus.AVAILABLE)
        );
        when(propertyService.getAvailableProperties()).thenReturn(ApiResponse.<List<PropertyResponse>>builder()
                .success(true).message("OK").data(mockProperties).build());

        // Cuando y Entonces
        mockMvc.perform(get("/alquila-seg/properties/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].propertyStatus").value("AVAILABLE"));
        verify(propertyService, times(1)).getAvailableProperties();
    }

    @Test
    @DisplayName("Debe obtener propiedades por categoría exitosamente")
    void getPropertiesByCategory_shouldReturnOk() throws Exception {
        // Dado
        List<PropertyResponse> mockProperties = Collections.singletonList(
                createMockPropertyResponse(1L, "Prop1 Casa", PropertyStatus.AVAILABLE)
        );
        when(propertyService.getPropertiesByCategory(anyString())).thenReturn(ApiResponse.<List<PropertyResponse>>builder()
                .success(true).message("OK").data(mockProperties).build());

        // Cuando y Entonces
        mockMvc.perform(get("/alquila-seg/properties/category/Casa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1));
        verify(propertyService, times(1)).getPropertiesByCategory("Casa");
    }

    @Test
    @DisplayName("Debe obtener propiedades por precio máximo exitosamente")
    void getPropertiesByMaxPrice_shouldReturnOk() throws Exception {
        // Dado
        List<PropertyResponse> mockProperties = Collections.singletonList(
                createMockPropertyResponse(1L, "Prop Barata", PropertyStatus.AVAILABLE)
        );
        when(propertyService.getPropertiesByMaxPrice(anyDouble())).thenReturn(ApiResponse.<List<PropertyResponse>>builder()
                .success(true).message("OK").data(mockProperties).build());

        // Cuando y Entonces
        mockMvc.perform(get("/alquila-seg/properties/max-price/100.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1));
        verify(propertyService, times(1)).getPropertiesByMaxPrice(100.0);
    }

    @Test
    @DisplayName("Debe obtener propiedades por ubicación exitosamente")
    void getPropertiesByLocation_shouldReturnOk() throws Exception {
        // Dado
        List<PropertyResponse> mockProperties = Collections.singletonList(
                createMockPropertyResponse(1L, "Prop BA", PropertyStatus.AVAILABLE)
        );
        when(propertyService.getPropertiesByLocation(anyString())).thenReturn(ApiResponse.<List<PropertyResponse>>builder()
                .success(true).message("OK").data(mockProperties).build());

        // Cuando y Entonces
        mockMvc.perform(get("/alquila-seg/properties/location/Buenos Aires"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1));
        verify(propertyService, times(1)).getPropertiesByLocation("Buenos Aires");
    }

    @Test
    @DisplayName("Debe obtener una propiedad por ID exitosamente")
    void getPropertyById_shouldReturnOk() throws Exception {
        // Dado
        PropertyResponse mockProperty = createMockPropertyResponse(1L, "Propiedad X", PropertyStatus.AVAILABLE);
        when(propertyService.getPropertyById(1L)).thenReturn(ApiResponse.<PropertyResponse>builder()
                .success(true).message("OK").data(mockProperty).build());

        // Cuando y Entonces
        mockMvc.perform(get("/alquila-seg/properties/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L));
        verify(propertyService, times(1)).getPropertyById(1L);
    }

    @Test
    @DisplayName("Debe retornar 404 si la propiedad por ID no es encontrada")
    void getPropertyById_shouldReturnNotFoundWhenNotExists() throws Exception {
        // Dado
        when(propertyService.getPropertyById(999L)).thenReturn(ApiResponse.<PropertyResponse>builder()
                .success(false).message("Propiedad con el id: 999 no encontrada.").build());

        // Cuando y Entonces
        mockMvc.perform(get("/alquila-seg/properties/{id}", 999L))
                .andExpect(status().isOk()) // Api response maneja 200 con success false
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Propiedad con el id: 999 no encontrada."));
        verify(propertyService, times(1)).getPropertyById(999L);
    }


    // --- POST Endpoint (Requiere ADMIN) ---

    @Test
    @DisplayName("Debe crear una propiedad como ADMIN exitosamente")
    @WithMockUser(roles = "ADMIN") // Simula un usuario con rol ADMIN
    void createProperty_shouldReturnOkForAdmin() throws Exception {
        // Dado
        PropertyCreateRequest request = createMockPropertyCreateRequest("Propiedad Creada", PropertyStatus.AVAILABLE);
        PropertyResponse response = createMockPropertyResponse(3L, "Propiedad Creada", PropertyStatus.AVAILABLE);

        when(propertyService.createProperty(any(PropertyCreateRequest.class))).thenReturn(ApiResponse.<PropertyResponse>builder()
                .success(true).message("OK").data(response).build());

        // Cuando y Entonces
        mockMvc.perform(post("/alquila-seg/properties")
                        .with(csrf()) // Necesario para peticiones POST/PUT/DELETE con seguridad
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Propiedad Creada"));
        verify(propertyService, times(1)).createProperty(any(PropertyCreateRequest.class));
    }

    @Test
    @DisplayName("Debe retornar 401 para usuario anónimo al intentar crear propiedad")
    @WithAnonymousUser // Simula un usuario no autenticado
    void createProperty_shouldReturnUnauthorizedForAnonymous() throws Exception {
        // Dado
        PropertyCreateRequest request = createMockPropertyCreateRequest("Propiedad Anónima", PropertyStatus.AVAILABLE);

        // Cuando y Entonces
        mockMvc.perform(post("/alquila-seg/properties")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized()); // Espera 401 UNAUTHORIZED
        verify(propertyService, never()).createProperty(any(PropertyCreateRequest.class));
    }


    @Test
    @DisplayName("Debe retornar 400 por validación al crear propiedad (ej. título vacío)")
    @WithMockUser(roles = "ADMIN")
    void createProperty_shouldReturnBadRequestForInvalidRequest() throws Exception {
        // Dado
        PropertyCreateRequest invalidRequest = createMockPropertyCreateRequest("", PropertyStatus.AVAILABLE); // Título vacío
        invalidRequest.setTitle("");

        // Cuando y Entonces
        mockMvc.perform(post("/alquila-seg/properties")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest()) // Espera 400 BAD REQUEST por validación
                .andExpect(jsonPath("$.message").exists()); // Verifica que hay un mensaje de error
        verify(propertyService, never()).createProperty(any(PropertyCreateRequest.class));
    }

    // --- PUT Endpoint (Requiere ADMIN) ---

    @Test
    @DisplayName("Debe actualizar una propiedad como ADMIN exitosamente")
    @WithMockUser(roles = "ADMIN")
    void updateProperty_shouldReturnOkForAdmin() throws Exception {
        // Dado
        PropertyCreateRequest request = createMockPropertyCreateRequest("Propiedad Actualizada", PropertyStatus.AVAILABLE);
        PropertyResponse response = createMockPropertyResponse(1L, "Propiedad Actualizada", PropertyStatus.AVAILABLE);

        when(propertyService.updateProperty(eq(1L), any(PropertyCreateRequest.class))).thenReturn(ApiResponse.<PropertyResponse>builder()
                .success(true).message("OK").data(response).build());

        // Cuando y Entonces
        mockMvc.perform(put("/alquila-seg/properties/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Propiedad Actualizada"));
        verify(propertyService, times(1)).updateProperty(eq(1L), any(PropertyCreateRequest.class));
    }

    @Test
    @DisplayName("Debe retornar 401 para usuario anónimo al intentar actualizar propiedad")
    @WithAnonymousUser
    void updateProperty_shouldReturnUnauthorizedForAnonymous() throws Exception {
        // Dado
        PropertyCreateRequest request = createMockPropertyCreateRequest("Propiedad Actualizada", PropertyStatus.AVAILABLE);

        // Cuando y Entonces
        mockMvc.perform(put("/alquila-seg/properties/{id}", 1L)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
        verify(propertyService, never()).updateProperty(eq(1L), any(PropertyCreateRequest.class));
    }


    @Test
    @DisplayName("Debe actualizar el estado de la propiedad como ADMIN exitosamente")
    @WithMockUser(roles = "ADMIN")
    void updatePropertyByStatus_shouldReturnOkForAdmin() throws Exception {
        // Dado
        PropertyResponse response = createMockPropertyResponse(1L, "Propiedad Estado", PropertyStatus.RESERVED);
        when(propertyService.updatePropertyByStatus(eq(1L), eq(PropertyStatus.RESERVED))).thenReturn(ApiResponse.<PropertyResponse>builder()
                .success(true).message("OK").data(response).build());

        // Cuando y Entonces
        mockMvc.perform(put("/alquila-seg/properties/{id}/status", 1L)
                        .with(csrf())
                        .param("status", "RESERVED") // Envía el enum como String
                        .contentType(MediaType.APPLICATION_JSON)) // Aunque no tenga body, es buena práctica
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.propertyStatus").value("RESERVED"));
        verify(propertyService, times(1)).updatePropertyByStatus(eq(1L), eq(PropertyStatus.RESERVED));
    }

    @Test
    @DisplayName("Debe retornar 401 para usuario anónimo al intentar actualizar estado de propiedad")
    @WithAnonymousUser
    void updatePropertyByStatus_shouldReturnUnauthorizedForAnonymous() throws Exception {
        // Cuando y Entonces
        mockMvc.perform(put("/alquila-seg/properties/{id}/status", 1L)
                        .with(csrf())
                        .param("status", "RESERVED"))
                .andExpect(status().isUnauthorized());
        verify(propertyService, never()).updatePropertyByStatus(anyLong(), any(PropertyStatus.class));
    }


    // --- DELETE Endpoint (Requiere ADMIN) ---

    @Test
    @DisplayName("Debe eliminar una propiedad como ADMIN exitosamente")
    @WithMockUser(roles = "ADMIN")
    void deleteProperty_shouldReturnOkForAdmin() throws Exception {
        // Dado
        when(propertyService.deleteProperty(1L)).thenReturn(ApiResponse.<Void>builder()
                .success(true).message("OK").build());

        // Cuando y Entonces
        mockMvc.perform(delete("/alquila-seg/properties/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        verify(propertyService, times(1)).deleteProperty(1L);
    }

    @Test
    @DisplayName("Debe retornar 401 para usuario anónimo al intentar eliminar propiedad")
    @WithAnonymousUser
    void deleteProperty_shouldReturnUnauthorizedForAnonymous() throws Exception {
        // Cuando y Entonces
        mockMvc.perform(delete("/alquila-seg/properties/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
        verify(propertyService, never()).deleteProperty(anyLong());
    }

}