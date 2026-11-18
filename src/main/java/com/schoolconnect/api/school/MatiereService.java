package com.schoolconnect.api.school;

import com.schoolconnect.api.school.MatiereDtos.CreateMatiereRequest;
import com.schoolconnect.api.school.MatiereDtos.MatiereView;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class MatiereService {

    private final MatiereRepository matieres;
    private final EtablissementRepository etablissements;

    public MatiereService(MatiereRepository matieres, EtablissementRepository etablissements) {
        this.matieres = matieres;
        this.etablissements = etablissements;
    }

    @Transactional
    public MatiereView create(UUID etablissementId, CreateMatiereRequest request) {
        if (!etablissements.existsById(etablissementId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Établissement introuvable");
        }
        String code = request.code().trim().toUpperCase(Locale.ROOT);
        if (matieres.existsByEtablissementIdAndCode(etablissementId, code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette matière existe déjà pour cet établissement");
        }
        Matiere m = new Matiere();
        m.setEtablissementId(etablissementId);
        m.setNom(request.nom().trim());
        m.setCode(code);
        m.setCoefficientDefaut(request.coefficientDefaut());
        return MatiereView.from(matieres.save(m));
    }

    @Transactional(readOnly = true)
    public List<MatiereView> list(UUID etablissementId) {
        return matieres.findByEtablissementIdOrderByNom(etablissementId).stream().map(MatiereView::from).toList();
    }
}
