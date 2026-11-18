package com.schoolconnect.api.school.repository;

import com.schoolconnect.api.school.model.Etablissement;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EtablissementRepository extends JpaRepository<Etablissement, UUID> {

    boolean existsByCode(String code);
}
