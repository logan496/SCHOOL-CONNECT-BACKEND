package com.schoolconnect.api.school.service;

import com.schoolconnect.api.school.dto.ClasseDtos.ClasseView;
import com.schoolconnect.api.school.dto.ClasseDtos.CreateClasseRequest;
import com.schoolconnect.api.school.model.Classe;
import com.schoolconnect.api.school.repository.AnneeScolaireRepository;
import com.schoolconnect.api.school.repository.ClasseRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ClasseService {

    private final ClasseRepository classes;
    private final AnneeScolaireRepository annees;

    public ClasseService(ClasseRepository classes, AnneeScolaireRepository annees) {
        this.classes = classes;
        this.annees = annees;
    }

    @Transactional
    public ClasseView create(UUID etablissementId, CreateClasseRequest request) {
        if (!annees.existsByIdAndEtablissementId(request.anneeScolaireId(), etablissementId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Année scolaire introuvable pour cet établissement");
        }
        String nom = request.nom().trim();
        if (classes.existsByAnneeScolaireIdAndNom(request.anneeScolaireId(), nom)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette classe existe déjà pour cette année scolaire");
        }
        Classe c = new Classe();
        c.setEtablissementId(etablissementId);
        c.setAnneeScolaireId(request.anneeScolaireId());
        c.setNom(nom);
        c.setNiveau(request.niveau().trim());
        c.setEffectifMax(request.effectifMax());
        return ClasseView.from(classes.save(c));
    }

    @Transactional(readOnly = true)
    public List<ClasseView> list(UUID etablissementId, UUID anneeScolaireId) {
        return classes.findByEtablissementIdAndAnneeScolaireIdOrderByNom(etablissementId, anneeScolaireId).stream()
                .map(ClasseView::from).toList();
    }
}
