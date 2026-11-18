package com.schoolconnect.api.paiement.repository;

import com.schoolconnect.api.paiement.model.Paiement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaiementRepository extends JpaRepository<Paiement, UUID> {

    List<Paiement> findByEleveIdAndAnneeScolaireIdOrderByDatePaiementDesc(UUID eleveId, UUID anneeScolaireId);

    Optional<Paiement> findByIdAndEtablissementId(UUID id, UUID etablissementId);

    List<Paiement> findByEtablissementIdAndDatePaiement(UUID etablissementId, LocalDate datePaiement);

    boolean existsByNumeroRecu(String numeroRecu);
}
