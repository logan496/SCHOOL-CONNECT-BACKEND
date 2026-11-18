package com.schoolconnect.api.school;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EtablissementRepository extends JpaRepository<Etablissement, UUID> {

    boolean existsByCode(String code);
}
