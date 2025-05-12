package org.novize.api.services;

import org.novize.api.dtos.NotificationDto;
import org.novize.api.enums.NotificationType;
import org.novize.api.mapper.NotificationMapper;
import org.novize.api.model.Notification;
import org.novize.api.model.User;
import org.novize.api.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    NotificationRepository notificationRepository;

    @Autowired
    SimpMessagingTemplate messagingTemplate;

    @Autowired
    NotificationMapper notificationMapper;

    /**
     * Sendet eine Benachrichtigung an einen Benutzer
     */
    public NotificationDto sendNotification(User recipient, NotificationType type, String message, String payload) {
        // Benachrichtigung in der Datenbank speichern
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setMessage(message);
        notification.setPayload(payload);

        notification = notificationRepository.save(notification);

        // NotificationDto erstellen
        NotificationDto notificationDto = notificationMapper.toDto(notification);


        // Echtzeit-Benachrichtigung über WebSocket senden
        messagingTemplate.convertAndSendToUser(
                recipient.getEmail(),
                "/queue/notifications",
                notificationDto
        );

        return notificationDto;
    }

    /**
     * Holt alle ungelesenen Benachrichtigungen für einen Benutzer
     */
    public List<NotificationDto> getUnreadNotifications(User user) {
        var notifications =  notificationRepository.findByRecipientAndReadFalseOrderByCreatedAtDesc(user);
        return notificationMapper.toDtoList(notifications);    }

    /**
     * Markiert eine Benachrichtigung als gelesen
     */
    public void markAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Benachrichtigung nicht gefunden"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    /**
     * Markiert alle Benachrichtigungen eines Benutzers als gelesen
     */
    public void markAllAsRead(User user) {
        List<Notification> notifications = notificationRepository.findByRecipientAndReadFalse(user);
        notifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(notifications);
    }

    public void deleteNotification(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Benachrichtigung nicht gefunden"));
        notificationRepository.delete(notification);
    }
}
