package com.schoolconnect.api.note.repository;

import com.schoolconnect.api.note.model.Note;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, UUID> {

    List<Note> findByEvaluationId(UUID evaluationId);

    Optional<Note> findByEvaluationIdAndEleveId(UUID evaluationId, UUID eleveId);

    List<Note> findByEleveId(UUID eleveId);
}
