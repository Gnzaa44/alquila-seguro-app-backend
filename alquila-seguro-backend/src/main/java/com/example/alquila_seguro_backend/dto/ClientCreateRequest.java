package com.example.alquila_seguro_backend.dto;

import com.example.alquila_seguro_backend.validation.ArgentinianPhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientCreateRequest {
    @NotBlank(message = "First name is required")
    @Size(min = 3, max = 100, message = "First name must be between 3 and 100 characters long")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 3, max = 100, message = "Last name must be between 3 and 100 characters long")
    private String lastName;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @ArgentinianPhoneNumber(message = "Phone number must be a valid Argentinian phone number")
    @NotBlank(message = "Phone is required")
    @Size(min = 10, max = 15, message = "Phone must be between 10 and 15 characters long")
    private String phone;
}
