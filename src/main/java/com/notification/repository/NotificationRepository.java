package com.notification.repository;

import com.notification.model.NotificationLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends MongoRepository<NotificationLog, String> {

    /**
     * Find notification by our custom ID
     */
    Optional<NotificationLog> findByNotificationId(String notificationId);

    /**
     * Find all notifications for a recipient
     */
    List<NotificationLog> findByRecipient(String recipient);

    /**
     * Find notifications by type
     */
    List<NotificationLog> findByType(String type);

    /**
     * Find notifications by status
     */
    List<NotificationLog> findByStatus(String status);

    /**
     * Find notifications by intent
     */
    List<NotificationLog> findByIntent(String intent);

    /**
     * Find recent notifications
     */
    List<NotificationLog> findTop10ByOrderByCreatedAtDesc();

    /**
     * Find notifications created after a date
     */
    List<NotificationLog> findByCreatedAtAfter(LocalDateTime date);
}