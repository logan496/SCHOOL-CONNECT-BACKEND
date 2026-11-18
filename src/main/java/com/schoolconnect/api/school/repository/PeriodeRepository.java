package com.schoolconnect.api.school.repository;

import com.schoolconnect.api.school.model.Periode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PeriodeRepository extends JpaRepository<Periode, UUID> {

    List<Periode> findByAnneeScolaireIdOrderByTypeAscNumeroAsc(UUID anneeScolaireId);

    Optional<Periode> findByIdAndAnneeScolaireId(UUID id, UUID anneeScolaireId);

    boolean existsByAnneeScolaireIdAndTypeAndNumero(UUID anneeScolaireId, com.schoolconnect.api.school.model.TypePeriode type, int numero);
}
