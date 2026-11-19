package com.schoolconnect.api.eleve.service;

import com.schoolconnect.api.eleve.dto.EleveDtos.EnfantView;
import com.schoolconnect.api.eleve.dto.EleveDtos.LierParCodeRequest;
import com.schoolconnect.api.eleve.dto.EleveDtos.ParentEleveView;
import com.schoolconnect.api.eleve.model.CodeLiaison;
import com.schoolconnect.api.eleve.model.Eleve;
import com.schoolconnect.api.eleve.model.ParentEleve;
import com.schoolconnect.api.eleve.model.StatutParentEleve;
import com.schoolconnect.api.eleve.repository.CodeLiaisonRepository;
import com.schoolconnect.api.eleve.repository.EleveRepository;
import com.schoolconnect.api.eleve.repository.ParentEleveRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Logique métier côté parent : rattachement à un élève via code de liaison,
 * consultation de la liste des enfants liés.
 */
@Service
public class ParentService {

    private final ParentEleveRepository parentsEleves;
    private final CodeLiaisonRepository codesLiaison;
    private final EleveRepository eleves;

    public ParentService(ParentEleveRepository parentsEleves,
                         CodeLiaisonRepository codesLiaison,
                         EleveRepository eleves) {
        this.parentsEleves = parentsEleves;
        this.codesLiaison = codesLiaison;
        this.eleves = eleves;
    }

    /**
     * CU-PAR-01 : le parent saisit le code de liaison remis par le secrétariat
     * pour se rattacher à un élève. Le lien est créé en statut EN_ATTENTE
     * et devra être validé par le secrétariat (CU-SEC-05).
     */
    @Transactional
    public ParentEleveView lierParCode(UUID parentId, LierParCodeRequest request) {
        String code = request.code().trim().toUpperCase();

        CodeLiaison codeLiaison = codesLiaison.findByCode(code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Code de liaison invalide ou introuvable"));

        if (codeLiaison.isUtilise()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce code de liaison a déjà été utilisé");
        }
        if (codeLiaison.getExpireLe().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Ce code de liaison a expiré");
        }

        // Vérifier que le parent n'est pas déjà lié à cet élève
        if (parentsEleves.findByUtilisateurIdAndEleveId(parentId, codeLiaison.getEleveId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vous êtes déjà lié(e) à cet élève");
        }

        // Marquer le code comme utilisé
        codeLiaison.setUtilise(true);
        codeLiaison.setUtiliseParId(parentId);
        codeLiaison.setUtiliseLe(Instant.now());
        codesLiaison.save(codeLiaison);

        // Créer le lien parent-élève en attente de validation
        ParentEleve lien = new ParentEleve();
        lien.setUtilisateurId(parentId);
        lien.setEleveId(codeLiaison.getEleveId());
        lien.setLien(codeLiaison.getLien());
        lien.setStatut(StatutParentEleve.EN_ATTENTE);

        return ParentEleveView.from(parentsEleves.save(lien));
    }

    /** CU-PAR-02 : liste tous les enfants rattachés au parent connecté. */
    @Transactional(readOnly = true)
    public List<EnfantView> listerEnfants(UUID parentId) {
        List<ParentEleve> liens = parentsEleves.findByUtilisateurId(parentId);

        return liens.stream().map(lien -> {
            Eleve eleve = eleves.findById(lien.getEleveId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Élève introuvable"));
            return new EnfantView(
                    lien.getId(),
                    eleve.getId(),
                    eleve.getNom(),
                    eleve.getPrenom(),
                    lien.getLien(),
                    lien.isPeutConsulter(),
                    lien.isPeutPayer(),
                    lien.isPeutJustifier(),
                    lien.getStatut()
            );
        }).toList();
    }

    /** CU-PAR-03 : détail d'un enfant rattaché (vérifie que le lien est validé). */
    @Transactional(readOnly = true)
    public EnfantView consulterEnfant(UUID parentId, UUID eleveId) {
        ParentEleve lien = parentsEleves.findByUtilisateurIdAndEleveId(parentId, eleveId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aucun lien trouvé avec cet élève"));
        if (lien.getStatut() != StatutParentEleve.VALIDE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Votre lien avec cet élève n'est pas encore validé");
        }
        Eleve eleve = eleves.findById(eleveId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Élève introuvable"));
        return new EnfantView(
                lien.getId(),
                eleve.getId(),
                eleve.getNom(),
                eleve.getPrenom(),
                lien.getLien(),
                lien.isPeutConsulter(),
                lien.isPeutPayer(),
                lien.isPeutJustifier(),
                lien.getStatut()
        );
    }
}
