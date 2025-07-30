package com.example.alquila_seguro_backend.services;


import com.example.alquila_seguro_backend.dto.ConsultancyResponse;
import com.example.alquila_seguro_backend.dto.ReservationResponse;
import com.example.alquila_seguro_backend.entity.*;
import com.example.alquila_seguro_backend.repositories.ContractRepository;
import com.example.alquila_seguro_backend.repositories.InvoiceRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class EmailService {
    private final static Logger LOGGER = LoggerFactory.getLogger(EmailService.class.getName());

    private final JavaMailSender mailSender;
    private final InvoiceRepository invoiceRepository;
    private final ContractRepository contractRepository;

    public void sendEmail(String to, String subject, String body) throws MessagingException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        try {
            mailSender.send(message);
        } catch (MailException e) {
            LOGGER.error("Error al enviar el email simple a: {}", to, e);
            throw e;
        }
    }
    public void sendEmailWithAttachment(String to, String subject, String body, Resource attachment) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(body);
            mimeMessageHelper.addAttachment(attachment.getFilename(), attachment);
            mailSender.send(mimeMessage);
            LOGGER.info("Email enviado a: {} ", to);
        } catch (MessagingException e) {
            LOGGER.error("Error al enviar el email a: {}", to, e);
        }catch (MailException e) {
            LOGGER.error("Error al enviar el email a: {}", to, e);
        }
    }
    public void sendReservationConfirmation(Reservation reservation) {
        String subject = "Confirmación de reserva";
        String body = "Su reserva ha sido confirmada. Adjuntamos los archivos PDF de la factura y el contrato.";
        try {
            Invoice invoice = reservation.getInvoice();
            Contract contract = reservation.getContract();

            if (invoice != null && contract != null) {
                Resource invoiceFile = null;
                Resource contractFile = null;
                invoiceFile = new ClassPathResource(invoice.getFilePath());
                contractFile = new ClassPathResource(contract.getFilePath());

                sendEmailWithAttachment(reservation.getClient().getEmail(), subject, body, invoiceFile);
                sendEmailWithAttachment(reservation.getClient().getEmail(), subject, body, contractFile);

                invoice.setStatus(DocumentStatus.SENT);
                invoiceRepository.save(invoice);
                contract.setStatus(DocumentStatus.SENT);
                contractRepository.save(contract);

                LOGGER.info("Estados de factura {} y contrato {} actualizados a SENT.", invoice.getId(), contract.getId());

            } else {
                LOGGER.warn("Factura o contrato no encontrado para la reserva: {}. No se enviará email de confirmación.", reservation.getId());
            }
        } catch (Exception e) {
            LOGGER.error("Error al enviar el email de confirmación para la reserva: {}", reservation.getId(), e);
        }
    }


    public void sendConsultancyPaidToVeedor(Consultancy consultancy, String veedorEmail) {
        String subject = "Nueva consultoría pagada";
        String body = "Se ha aprobado el pago de una consultoría.\n\n"
                + "Cliente: " + consultancy.getClient().getFirstName() + " " + consultancy.getClient().getLastName() + "\n"
                + "Email: " + consultancy.getClient().getEmail() + "\n"
                + "Teléfono: " + consultancy.getClient().getPhone() + "\n"
                + "Detalles: " + consultancy.getDetails() + "\n";
        try {
            sendEmail(veedorEmail, subject, body);
        } catch (Exception e) {
            LOGGER.error("Error al enviar el email de consultoría pagada al veedor: {}", veedorEmail, e);
        }
    }
    public void sendConsultancyPaidToClient(Consultancy consultancy) {
        String subject = "¡Pago de consultoría recibido!";
        String body = "Hola " + consultancy.getClient().getFirstName() + ",\n\n"
                + "Tu pago de consultoría fue aprobado. Un veedor se pondrá en contacto contigo pronto.\n\n"
                + "Detalles de tu consulta:\n" + consultancy.getDetails() + "\n\n"
                + "¡Gracias por confiar en nosotros!";
        try {
            sendEmail(consultancy.getClient().getEmail(), subject, body);
        } catch (Exception e) {
            LOGGER.error("Error al enviar el email de confirmación de consultoría al cliente: {}", consultancy.getClient().getEmail(), e);
        }
    }
}
