package com.schoolconnect.api.school.repository;

import com.schoolconnect.api.school.model.Etablissement;
import com.schoolconnect.api.school.model.StatutEtablissement;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EtablissementRepository extends JpaRepository<Etablissement, UUID> {

    boolean existsByCode(String code);

    List<Etablissement> findByStatut(StatutEtablissement statut);
}
