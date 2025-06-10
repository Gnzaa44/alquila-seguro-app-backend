package com.example.alquila_seguro_backend;

import com.example.alquila_seguro_backend.dto.JwtResponse;
import com.example.alquila_seguro_backend.dto.LoginRequest;
import com.example.alquila_seguro_backend.entity.Admin;
import com.example.alquila_seguro_backend.repositories.AdminRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put; // Para los tests de PUT de reservas
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Activa el perfil 'test' para usar la configuración de H2
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminUsername = "testadmin";
    private String adminPassword = "securepassword";
    private String encodedAdminPassword;

    // Clase auxiliar para deserializar la respuesta de Login que está envuelta en ApiResponse
    static class LoginResponseWrapper {
        public boolean success;
        public String message;
        public JwtResponse data; // El campo 'data' contendrá el JwtResponse

        public JwtResponse getData() {
            return data;
        }
    }

    @BeforeEach
    void setUp() {
        // Limpiar la base de datos antes de cada test
        adminRepository.deleteAll();

        // Crear un administrador de prueba para usar en los tests
        encodedAdminPassword = passwordEncoder.encode(adminPassword);
        Admin admin = Admin.builder()
                .username(adminUsername)
                .password(encodedAdminPassword)
                .build();
        adminRepository.save(admin);
    }

    @Test
    @DisplayName("Login de administrador exitoso debería devolver un token JWT")
    void testAdminLogin_success() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .username(adminUsername)
                .password(adminPassword)
                .build();

        MvcResult result = mockMvc.perform(post("/alquila-seg/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Autenticación exitosa"))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.username").value(adminUsername))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andReturn();

        // Extraer el token para usarlo en otros tests, si fuera necesario
        String jsonResponse = result.getResponse().getContentAsString();
        LoginResponseWrapper responseWrapper = objectMapper.readValue(jsonResponse, LoginResponseWrapper.class);
        assertThat(responseWrapper.getData().getToken()).isNotBlank();
    }

    @Test
    @DisplayName("Login de administrador con credenciales incorrectas debería devolver 401 Unauthorized")
    void testAdminLogin_badCredentials() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .username("wronguser")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/alquila-seg/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.message").value("Credenciales incorrectas."));
    }

    @Test
    @DisplayName("Acceder a un endpoint de administrador sin autenticación debería devolver 401 Unauthorized")
    void testAdminEndpoint_noAuth_shouldReturnUnauthorized() throws Exception {
        // Usamos un endpoint de admin real como ejemplo, por ejemplo, /admin/test-resource
        // Si no tienes un AdminController con /admin/test-resource, usa uno de tus PUT de reservas
        mockMvc.perform(get("/admin/test-resource"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Acceder a un endpoint de administrador con token JWT válido debería devolver 200 OK")
    void testAdminEndpoint_withValidJwt_shouldReturnOk() throws Exception {
        // Primero, logearse para obtener un token JWT
        LoginRequest loginRequest = LoginRequest.builder()
                .username(adminUsername)
                .password(adminPassword)
                .build();

        MvcResult loginResult = mockMvc.perform(post("/alquila-seg/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String jsonResponse = loginResult.getResponse().getContentAsString();
        LoginResponseWrapper responseWrapper = objectMapper.readValue(jsonResponse, LoginResponseWrapper.class);
        String jwtToken = responseWrapper.getData().getToken();

        // Ahora, usar el token para acceder a un endpoint protegido
        // Reemplaza "/admin/test-resource" con un endpoint real de tu AdminController
        // o con uno de los PUT de reservas que requieren ROLE_ADMIN (por ejemplo, /alquila-seg/reservations/1/confirm)
        mockMvc.perform(put("/alquila-seg/reservations/{id}/cancel", 1) // O "/alquila-seg/reservations/{id}/cancel"
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk()); // Asegúrate de que el controlador de reservas devuelva 200 OK para un PUT exitoso
    }

    @Test
    @DisplayName("Acceder a un endpoint público debería devolver 200 OK sin autenticación")
    void testPublicEndpoint_noAuth_shouldReturnOk() throws Exception {
        // Usa una ruta GET pública real de tu SecurityConfig, por ejemplo, /alquila-seg/properties/1
        mockMvc.perform(get("/alquila-seg/properties/1"))
                .andExpect(status().isOk());
    }

    /*
    // --- ESTE TEST ES OPCIONAL ---
    // Si tu aplicación tiene un endpoint para registrar nuevos administradores, puedes descomentar y usar este test.
    // Necesitarías un AuthController con un método para el registro de administradores, por ejemplo:
    // @PostMapping("/register-admin")
    // public ResponseEntity<?> registerAdmin(@Valid @RequestBody RegisterAdminRequest request) { ... }
    @Test
    @DisplayName("Registro de administrador debería ser exitoso (si existe endpoint)")
    void testAdminRegistration_success() throws Exception {
        RegisterAdminRequest registerRequest = RegisterAdminRequest.builder()
                .username("newadminuser")
                .password("supersecurepassword")
                .build();

        mockMvc.perform(post("/alquila-seg/auth/register-admin") // <--- Reemplaza con tu ruta de registro de admin
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk()); // O 201 Created si tu API lo devuelve

        // Opcional: Verificar que el admin se guardó en la DB
        assertThat(adminRepository.findByUsername("newadminuser")).isPresent();
    }
    */
}