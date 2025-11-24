package com.cpd.notification_service.listener;

import com.cpd.notification_service.model.VideoReceivedEvent;
import com.cpd.notification_service.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);
    private final EmailService emailService;

    public NotificationConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "video-completed-topic", groupId = "notification-group")
    public void consume(VideoReceivedEvent event) {
        log.info(">>> NOTIFICATION: Video processing finished. UUID: {}", event.getUuid());

        String subject = "Video Processed: " + event.getOriginalFilename();
        String body = String.format("""
            Olá!
            
            Seu vídeo foi processado com sucesso por nossa arquitetura de microsserviços.
            
            Detalhes:
            - ID: %s
            - Arquivo Original: %s
            - Local Final: %s
            
            Obrigado por usar o CPD Micro Video Processor!
            """, event.getUuid(), event.getOriginalFilename(), event.getFilePath());

        emailService.sendNotification(subject, body);
    }
}