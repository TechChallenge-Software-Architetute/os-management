package com.os.workshop.infrastructure.notification;

import com.os.workshop.application.notification.port.out.EmailNotificationPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Service
@Slf4j
public class SesEmailService implements EmailNotificationPort {

    private final SesClient sesClient;
    private final String senderEmail;
    private final EmailTemplateRenderer templateRenderer;

    public SesEmailService(
            SesClient sesClient,
            @Value("${aws.ses.sender-email:noreply@oficina.com}") String senderEmail,
            EmailTemplateRenderer templateRenderer) {
        this.sesClient = sesClient;
        this.senderEmail = senderEmail;
        this.templateRenderer = templateRenderer;
    }

    @Override
    @Async
    public void sendStatusUpdate(String toEmail, String clientName, String vehicleInfo, String newStatus) {
        try {
            String htmlBody = templateRenderer.render(clientName, vehicleInfo, newStatus);

            SendEmailRequest request = SendEmailRequest.builder()
                    .source(senderEmail)
                    .destination(Destination.builder().toAddresses(toEmail).build())
                    .message(Message.builder()
                            .subject(Content.builder()
                                    .data("OS Management - Atualizacao de Status: " + newStatus)
                                    .charset("UTF-8")
                                    .build())
                            .body(Body.builder()
                                    .html(Content.builder()
                                            .data(htmlBody)
                                            .charset("UTF-8")
                                            .build())
                                    .build())
                            .build())
                    .build();

            sesClient.sendEmail(request);
            log.info("Status update email sent to {} - Status: {}", toEmail, newStatus);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage(), e);
        }
    }
}
