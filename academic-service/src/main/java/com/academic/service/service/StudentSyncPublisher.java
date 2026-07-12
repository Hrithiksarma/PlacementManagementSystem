package com.academic.service.service;

import com.academic.service.dto.StudentSyncMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Publishes student-update events to Redis so PRMS can keep its imported
 * copy of a student in sync without either service polling the other.
 */
@Service
public class StudentSyncPublisher {

    private static final Logger log = LoggerFactory.getLogger(StudentSyncPublisher.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper        objectMapper;
    private final String              channel;

    public StudentSyncPublisher(StringRedisTemplate redisTemplate,
                                 ObjectMapper objectMapper,
                                 @Value("${academic.sync.channel}") String channel) {
        this.redisTemplate = redisTemplate;
        this.objectMapper  = objectMapper;
        this.channel       = channel;
    }

    public void publishUpdate(StudentSyncMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            redisTemplate.convertAndSend(channel, json);
        } catch (JsonProcessingException e) {
            // Sync is best-effort: a Redis/serialization hiccup must never
            // fail the student update itself, which already committed to MySQL.
            log.error("Failed to publish student sync event for rollNo={}", message.getRollNo(), e);
        }
    }
}
