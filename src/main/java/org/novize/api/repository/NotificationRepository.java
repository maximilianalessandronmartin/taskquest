package org.novize.api.repository;

import org.novize.api.model.Notification;
import org.novize.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);
    List<Notification> findByRecipientAndReadFalseOrderByCreatedAtDesc(User recipient);
    List<Notification> findByRecipientAndReadFalse(User recipient);
}