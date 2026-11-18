package com.schoolconnect.api.school;

import com.schoolconnect.api.school.EtablissementDtos.CreateEtablissementRequest;
import com.schoolconnect.api.school.EtablissementDtos.EtablissementView;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class EtablissementService {

    private final EtablissementRepository etablissements;

    public EtablissementService(EtablissementRepository etablissements) {
        this.etablissements = etablissements;
    }

    @Transactional
    public EtablissementView create(CreateEtablissementRequest request) {
        String code = request.code().trim().toUpperCase(Locale.ROOT);
        if (etablissements.existsByCode(code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce code d'établissement existe déjà");
        }
        Etablissement e = new Etablissement();
        e.setNom(request.nom().trim());
        e.setCode(code);
        e.setType(request.type());
        e.setSousSysteme(request.sousSysteme());
        e.setOrdreEnseignement(request.ordreEnseignement());
        e.setRegion(request.region());
        e.setDepartement(request.departement());
        e.setVille(request.ville());
        e.setAdresse(request.adresse());
        e.setTelephone(request.telephone());
        e.setEmail(request.email());
        return EtablissementView.from(etablissements.save(e));
    }

    @Transactional(readOnly = true)
    public List<EtablissementView> list() {
        return etablissements.findAll(Sort.by("nom")).stream().map(EtablissementView::from).toList();
    }

    @Transactional(readOnly = true)
    public EtablissementView get(UUID id) {
        return etablissements.findById(id)
                .map(EtablissementView::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Établissement introuvable"));
    }
}
