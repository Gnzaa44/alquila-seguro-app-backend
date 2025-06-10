package com.example.alquila_seguro_backend.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mail.MailSendException; // Importar la excepción correcta
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Mock
    private MimeMessage mockMimeMessage;

    @Mock
    private Resource mockAttachment;

    @BeforeEach
    void setUp() {
        // Eliminar el stub global de createMimeMessage aquí
        // Se moverá a los métodos de test específicos que lo necesitan.
    }

    @Test
    @DisplayName("Test sendEmail should send a simple email successfully")
    void testSendEmail_shouldSendSimpleEmailSuccessfully() throws MessagingException {
        // Given
        String to = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        // When
        assertDoesNotThrow(() -> emailService.sendEmail(to, subject, body));

        // Then
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage capturedMessage = messageCaptor.getValue();
        assertThat(capturedMessage.getTo()).containsExactly(to);
        assertThat(capturedMessage.getSubject()).isEqualTo(subject);
        assertThat(capturedMessage.getText()).isEqualTo(body);
    }

    @Test
    @DisplayName("Test sendEmail should throw MailSendException if mailSender fails")
    void testSendEmail_shouldThrowMailSendException_ifMailSenderFails() throws MessagingException {
        // Given
        String to = "fail@example.com";
        String subject = "Failing Subject";
        String body = "Failing Body";

        // Simular que mailSender.send() lanza una excepción de tipo MailSendException (RuntimeException)
        doThrow(new MailSendException("Error sending email"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // When & Then
        assertThrows(MailSendException.class, () -> emailService.sendEmail(to, subject, body));

        // Verificar que send() fue llamado
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Test sendEmailWithAttachment should send email with attachment successfully")
    void testSendEmailWithAttachment_shouldSendEmailWithAttachmentSuccessfully() throws MessagingException, IOException {
        // Given
        String to = "attach@example.com";
        String subject = "Attachment Subject";
        String body = "Attachment Body";

        // Mover este stub aquí, ya que solo este método lo usa
        when(mailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        when(mockAttachment.getFilename()).thenReturn("test_file.pdf");

        // When
        assertDoesNotThrow(() -> emailService.sendEmailWithAttachment(to, subject, body, mockAttachment));

        // Then
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mockMimeMessage);
    }

}