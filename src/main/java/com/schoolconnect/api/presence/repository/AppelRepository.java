package com.schoolconnect.api.presence.repository;

import com.schoolconnect.api.presence.model.Appel;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppelRepository extends JpaRepository<Appel, UUID> {

    Optional<Appel> findByClasseIdAndDateAppel(UUID classeId, LocalDate dateAppel);

    Optional<Appel> findByIdAndEtablissementId(UUID id, UUID etablissementId);

    boolean existsByClasseIdAndDateAppel(UUID classeId, LocalDate dateAppel);
}
