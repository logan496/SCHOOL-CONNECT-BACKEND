package com.schoolconnect.api.eleve;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EleveRepository extends JpaRepository<Eleve, UUID> {
}
