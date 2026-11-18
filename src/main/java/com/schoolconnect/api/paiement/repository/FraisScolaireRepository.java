package com.schoolconnect.api.paiement.repository;

import com.schoolconnect.api.paiement.model.FraisScolaire;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FraisScolaireRepository extends JpaRepository<FraisScolaire, UUID> {

    List<FraisScolaire> findByEtablissementIdAndAnneeScolaireIdAndNiveau(UUID etablissementId, UUID anneeScolaireId, String niveau);

    List<FraisScolaire> findByEtablissementIdAndAnneeScolaireId(UUID etablissementId, UUID anneeScolaireId);
}
