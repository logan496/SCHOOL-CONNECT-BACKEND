package com.schoolconnect.api.school.repository;

import com.schoolconnect.api.school.model.Matiere;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatiereRepository extends JpaRepository<Matiere, UUID> {

    List<Matiere> findByEtablissementIdOrderByNom(UUID etablissementId);

    boolean existsByEtablissementIdAndCode(UUID etablissementId, String code);
}
