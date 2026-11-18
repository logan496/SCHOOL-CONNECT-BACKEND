package com.schoolconnect.api.note.repository;

import com.schoolconnect.api.note.model.Evaluation;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationRepository extends JpaRepository<Evaluation, UUID> {

    Optional<Evaluation> findByIdAndEtablissementId(UUID id, UUID etablissementId);
}
