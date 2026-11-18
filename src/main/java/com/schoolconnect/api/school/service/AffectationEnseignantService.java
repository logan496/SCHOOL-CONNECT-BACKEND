package com.schoolconnect.api.school.service;

import com.schoolconnect.api.identity.model.RoleType;
import com.schoolconnect.api.identity.repository.RoleUtilisateurRepository;
import com.schoolconnect.api.school.dto.AffectationEnseignantDtos.AffectationView;
import com.schoolconnect.api.school.dto.AffectationEnseignantDtos.CreateAffectationRequest;
import com.schoolconnect.api.school.model.AffectationEnseignant;
import com.schoolconnect.api.school.repository.AffectationEnseignantRepository;
import com.schoolconnect.api.school.repository.AnneeScolaireRepository;
import com.schoolconnect.api.school.repository.ClasseRepository;
import com.schoolconnect.api.school.repository.MatiereRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class AffectationEnseignantService {

    private final AffectationEnseignantRepository affectations;
    private final ClasseRepository classes;
    private final MatiereRepository matieres;
    private final AnneeScolaireRepository annees;
    private final RoleUtilisateurRepository roles;

    public AffectationEnseignantService(AffectationEnseignantRepository affectations, ClasseRepository classes,
                                        MatiereRepository matieres, AnneeScolaireRepository annees,
                                        RoleUtilisateurRepository roles) {
        this.affectations = affectations;
        this.classes = classes;
        this.matieres = matieres;
        this.annees = annees;
        this.roles = roles;
    }

    /** La direction affecte un enseignant à une classe pour une matière (CU-ENS-02). */
    @Transactional
    public AffectationView creer(UUID etablissementId, CreateAffectationRequest request) {
        if (!classes.existsByIdAndEtablissementId(request.classeId(), etablissementId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Classe introuvable pour cet établissement");
        }
        if (!matieres.existsByIdAndEtablissementId(request.matiereId(), etablissementId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Matière introuvable pour cet établissement");
        }
        if (!roles.existsByUtilisateurIdAndEtablissementIdAndRoleAndActifTrue(
                request.enseignantId(), etablissementId, RoleType.ENSEIGNANT)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cet utilisateur n'a pas le rôle ENSEIGNANT dans cet établissement");
        }
        UUID anneeId = annees.findByEtablissementIdAndCouranteTrue(etablissementId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Aucune année scolaire courante définie pour cet établissement"))
                .getId();
        if (affectations.existsByAnneeScolaireIdAndClasseIdAndMatiereIdAndEnseignantId(
                anneeId, request.classeId(), request.matiereId(), request.enseignantId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette affectation existe déjà");
        }

        AffectationEnseignant a = new AffectationEnseignant();
        a.setEtablissementId(etablissementId);
        a.setAnneeScolaireId(anneeId);
        a.setClasseId(request.classeId());
        a.setMatiereId(request.matiereId());
        a.setEnseignantId(request.enseignantId());
        return AffectationView.from(affectations.save(a));
    }

    /** CU-ENS-02 : un enseignant consulte ses propres classes/matières pour l'année courante. */
    @Transactional(readOnly = true)
    public List<AffectationView> listPourEnseignant(UUID etablissementId, UUID enseignantId) {
        UUID anneeId = annees.findByEtablissementIdAndCouranteTrue(etablissementId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Aucune année scolaire courante définie pour cet établissement"))
                .getId();
        return affectations.findByEnseignantIdAndAnneeScolaireId(enseignantId, anneeId).stream()
                .map(AffectationView::from).toList();
    }
}
