package com.schoolconnect.api.presence.repository;

import com.schoolconnect.api.presence.model.PresenceEleve;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PresenceEleveRepository extends JpaRepository<PresenceEleve, UUID> {

    List<PresenceEleve> findByAppelId(UUID appelId);

    Optional<PresenceEleve> findByAppelIdAndEleveId(UUID appelId, UUID eleveId);

    List<PresenceEleve> findByEleveId(UUID eleveId);
}
