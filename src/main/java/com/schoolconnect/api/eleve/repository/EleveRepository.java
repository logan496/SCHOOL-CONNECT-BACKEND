package com.schoolconnect.api.eleve.repository;

import com.schoolconnect.api.eleve.model.Eleve;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EleveRepository extends JpaRepository<Eleve, UUID> {
}
