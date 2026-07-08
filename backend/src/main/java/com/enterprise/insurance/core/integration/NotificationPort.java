package com.enterprise.insurance.core.integration;

import java.util.Map;

/**
 * Port interface for sending notifications (SMS, Email, Push).
 */
public interface NotificationPort {

    /**
     * Send an SMS message.
     */
    NotificationResult sendSms(SmsRequest request);

    /**
     * Send an email.
     */
    NotificationResult sendEmail(EmailRequest request);

    /**
     * Send a push notification.
     */
    NotificationResult sendPush(PushRequest request);

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class SmsRequest {
        private String to;
        private String message;
        private String language; // ar, en
        private String senderName;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class EmailRequest {
        private String to;
        private String subject;
        private String body;
        private String templateCode;
        private Map<String, Object> templateData;
        private String language;
        private boolean hasAttachment;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class PushRequest {
        private String deviceToken;
        private String title;
        private String body;
        private Map<String, String> data;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    class NotificationResult {
        private boolean success;
        private String notificationId;
        private String errorMessage;
    }
}
