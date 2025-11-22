package com.cpd.transcoder_service.listener;

import com.cpd.transcoder_service.model.VideoReceivedEvent;
import com.cpd.transcoder_service.service.VideoConversionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class VideoConsumer {

    private static final Logger log = LoggerFactory.getLogger(VideoConsumer.class);
    private final VideoConversionService conversionService;

    public VideoConsumer(VideoConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @KafkaListener(topics = "video-received-topic", groupId = "transcoder-group")
    public void consumeVideoEvent(VideoReceivedEvent event) {
        log.info(">>> MESSAGE RECEIVED: Processing video UUID: {}", event.getUuid());

        File inputFile = new File(event.getFilePath());
        if (!inputFile.exists()) {
            log.error("File not found at path: {}. Is the volume mounted correctly?", event.getFilePath());
            return;
        }

        try {
            String resultPath = conversionService.convertVideo(event.getFilePath(), "720p");

            log.info(">>> PROCESSING COMPLETE. Transcoded file: {}", resultPath);

        } catch (Exception e) {
            log.error("Failed to process video {}", event.getUuid(), e);
        }
    }
}
