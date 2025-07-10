package team03.mopl.domain.notification.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import team03.mopl.domain.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

  List<Notification> findByReceiverIdOrderByCreatedAtDesc(UUID receiverId);

  List<Notification> findByReceiverIdAndIsRead(UUID receiverId, boolean read);

  void deleteByReceiverIdAndIsRead(UUID receiverId, boolean read);
}
