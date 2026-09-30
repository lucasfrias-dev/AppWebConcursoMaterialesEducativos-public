package ar.edu.unnoba.appweb_concurso_materiales_educativos.service.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username:ced.prueba123@gmail.com}")
    private String fromEmail;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    /**
     * Envía un correo electrónico de forma asíncrona para no bloquear el hilo de ejecución principal.
     * Si ocurre un error (p.ej. credenciales inválidas o servidor SMTP caído), se registra en log
     * sin interrumpir la operación del usuario.
     *
     * @param toEmail Dirección de correo electrónico del destinatario.
     * @param subject Asunto del correo electrónico.
     * @param body    Cuerpo del correo electrónico.
     */
    @Async
    public void sendEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setFrom(fromEmail);
            message.setText(body);
            message.setSubject(subject);

            javaMailSender.send(message);
            logger.info("Correo electrónico enviado exitosamente a: {}", toEmail);
        } catch (Exception e) {
            logger.error("No se pudo enviar el correo a {}. Motivo: {}", toEmail, e.getMessage());
        }
    }
}
