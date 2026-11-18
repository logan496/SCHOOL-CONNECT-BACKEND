package com.schoolconnect.api.school;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MatiereRepository extends JpaRepository<Matiere, UUID> {

    List<Matiere> findByEtablissementIdOrderByNom(UUID etablissementId);

    boolean existsByEtablissementIdAndCode(UUID etablissementId, String code);
}
