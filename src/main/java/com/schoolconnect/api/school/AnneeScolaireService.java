package com.schoolconnect.api.school;

import com.schoolconnect.api.school.AnneeScolaireDtos.AnneeScolaireView;
import com.schoolconnect.api.school.AnneeScolaireDtos.CreateAnneeScolaireRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class AnneeScolaireService {

    private final AnneeScolaireRepository annees;
    private final EtablissementRepository etablissements;

    public AnneeScolaireService(AnneeScolaireRepository annees, EtablissementRepository etablissements) {
        this.annees = annees;
        this.etablissements = etablissements;
    }

    @Transactional
    public AnneeScolaireView create(UUID etablissementId, CreateAnneeScolaireRequest request) {
        if (!etablissements.existsById(etablissementId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Établissement introuvable");
        }
        if (!request.dateFin().isAfter(request.dateDebut())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La date de fin doit être après la date de début");
        }
        if (annees.existsByEtablissementIdAndLibelle(etablissementId, request.libelle())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette année scolaire existe déjà pour cet établissement");
        }

        if (request.courante()) {
            annees.findByEtablissementIdAndCouranteTrue(etablissementId).ifPresent(previous -> {
                previous.setCourante(false);
                annees.save(previous);
            });
        }

        AnneeScolaire a = new AnneeScolaire();
        a.setEtablissementId(etablissementId);
        a.setLibelle(request.libelle().trim());
        a.setDateDebut(request.dateDebut());
        a.setDateFin(request.dateFin());
        a.setCourante(request.courante());
        return AnneeScolaireView.from(annees.save(a));
    }

    @Transactional(readOnly = true)
    public List<AnneeScolaireView> list(UUID etablissementId) {
        return annees.findByEtablissementIdOrderByDateDebutDesc(etablissementId).stream()
                .map(AnneeScolaireView::from).toList();
    }
}
