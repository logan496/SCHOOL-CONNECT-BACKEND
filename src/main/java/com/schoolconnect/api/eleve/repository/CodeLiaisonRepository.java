package com.schoolconnect.api.eleve.repository;

import com.schoolconnect.api.eleve.model.CodeLiaison;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeLiaisonRepository extends JpaRepository<CodeLiaison, UUID> {

    boolean existsByCode(String code);
}
