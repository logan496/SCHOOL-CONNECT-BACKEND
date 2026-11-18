package com.schoolconnect.api.notification.repository;

import com.schoolconnect.api.notification.model.Notification;
import com.schoolconnect.api.notification.model.TypeNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByUtilisateurIdOrderByCreatedAtDesc(UUID utilisateurId, Pageable pageable);

    Page<Notification> findByUtilisateurIdAndLueLeIsNullOrderByCreatedAtDesc(UUID utilisateurId, Pageable pageable);

    long countByUtilisateurIdAndLueLeIsNull(UUID utilisateurId);

    Optional<Notification> findByIdAndUtilisateurId(UUID id, UUID utilisateurId);

    boolean existsByEleveIdAndTypeAndCreatedAtAfter(UUID eleveId, TypeNotification type, Instant after);

    @Modifying
    @Query("update Notification n set n.lueLe = :lueLe where n.utilisateurId = :utilisateurId and n.lueLe is null")
    int marquerToutesLues(@Param("utilisateurId") UUID utilisateurId, @Param("lueLe") Instant lueLe);
}
