package com.schoolconnect.api.school.service;

import com.schoolconnect.api.school.dto.PeriodeDtos.CreatePeriodeRequest;
import com.schoolconnect.api.school.dto.PeriodeDtos.PeriodeView;
import com.schoolconnect.api.school.model.Periode;
import com.schoolconnect.api.school.repository.AnneeScolaireRepository;
import com.schoolconnect.api.school.repository.PeriodeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class PeriodeService {

    private final PeriodeRepository periodes;
    private final AnneeScolaireRepository annees;

    public PeriodeService(PeriodeRepository periodes, AnneeScolaireRepository annees) {
        this.periodes = periodes;
        this.annees = annees;
    }

    /** CU-DIR-02 : paramétrer les séquences/trimestres d'une année scolaire. */
    @Transactional
    public PeriodeView create(UUID etablissementId, UUID anneeScolaireId, CreatePeriodeRequest request) {
        if (!annees.existsByIdAndEtablissementId(anneeScolaireId, etablissementId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Année scolaire introuvable pour cet établissement");
        }
        if (!request.dateFin().isAfter(request.dateDebut())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La date de fin doit être après la date de début");
        }
        if (periodes.existsByAnneeScolaireIdAndTypeAndNumero(anneeScolaireId, request.type(), request.numero())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette période existe déjà pour cette année scolaire");
        }
        if (request.periodeParentId() != null
                && periodes.findByIdAndAnneeScolaireId(request.periodeParentId(), anneeScolaireId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Période parente introuvable pour cette année scolaire");
        }

        Periode p = new Periode();
        p.setAnneeScolaireId(anneeScolaireId);
        p.setType(request.type());
        p.setNumero(request.numero());
        p.setPeriodeParentId(request.periodeParentId());
        p.setDateDebut(request.dateDebut());
        p.setDateFin(request.dateFin());
        return PeriodeView.from(periodes.save(p));
    }

    @Transactional(readOnly = true)
    public List<PeriodeView> list(UUID etablissementId, UUID anneeScolaireId) {
        if (!annees.existsByIdAndEtablissementId(anneeScolaireId, etablissementId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Année scolaire introuvable pour cet établissement");
        }
        return periodes.findByAnneeScolaireIdOrderByTypeAscNumeroAsc(anneeScolaireId).stream()
                .map(PeriodeView::from).toList();
    }

    /** Ouvre ou ferme la saisie des notes pour une période (prérequis à CU-ENS-04/05). */
    @Transactional
    public PeriodeView setSaisieOuverte(UUID etablissementId, UUID anneeScolaireId, UUID periodeId, boolean ouverte) {
        if (!annees.existsByIdAndEtablissementId(anneeScolaireId, etablissementId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Année scolaire introuvable pour cet établissement");
        }
        Periode p = periodes.findByIdAndAnneeScolaireId(periodeId, anneeScolaireId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Période introuvable pour cette année scolaire"));
        p.setSaisieOuverte(ouverte);
        return PeriodeView.from(periodes.save(p));
    }
}
