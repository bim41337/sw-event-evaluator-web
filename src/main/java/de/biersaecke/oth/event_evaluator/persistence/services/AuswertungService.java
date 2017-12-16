package de.biersaecke.oth.event_evaluator.persistence.services;

import de.biersaecke.oth.event_evaluator.persistence.entities.Auswertung;
import de.biersaecke.oth.event_evaluator.persistence.entities.Benutzer;
import de.biersaecke.oth.event_evaluator.persistence.entities.Eintrag;
import de.biersaecke.oth.event_evaluator.persistence.utils.api.OptionAuswertung;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import org.apache.logging.log4j.Logger;

/**
 * Service zum Erstellen und Persistieren von Auswertungen
 *
 * @author bim41337
 */
@RequestScoped
public class AuswertungService {

    @PersistenceContext( unitName = "eventevalPU" )
    private EntityManager entityManager;

    @Inject
    private KalenderService kalenderService;

    @Inject
    @OptionAuswertung
    private Logger logger;

    @Transactional
    public Auswertung fuellenAuswertung(Auswertung auswertung) {
        List<Eintrag> postenList = kalenderService
                .suchenEintraegeFuerUser(null, auswertung.getZeitraum(), auswertung
                        .getBenutzer());
        Collections.sort(postenList);
        postenList.forEach(auswertung::hinzufuegenPosten);

        return auswertung;
    }

    @Transactional
    public Auswertung speichernAuswertung(Auswertung auswertung) {
        entityManager.persist(auswertung);

        logger.info("Neue Auswertung: " + auswertung);
        return auswertung;
    }

    @Transactional
    public Auswertung holenAuswertung(Long auswertungId) {
        Auswertung auswertung = entityManager
                .find(Auswertung.class, auswertungId);

        if (auswertung == null) {
            throw new RuntimeException("Ungültige Auswertung-ID");
        }

        return auswertung;
    }

    @Transactional
    public List<Auswertung> holenAuswertungenFuerBenutzer(Long benutzerId) {
        TypedQuery<Auswertung> auswQuery = entityManager.
                createNamedQuery(Auswertung.NQ_PARAMS_AUSWERTUNG_NUTZER, Auswertung.class);

        auswQuery.setParameter("user", entityManager.
                find(Benutzer.class, benutzerId));

        return auswQuery.getResultList();
    }

    @Transactional
    public void loeschenAuswertung(Auswertung auswertung) {
        auswertung = entityManager.merge(auswertung);
        for (Eintrag posten : auswertung.getPosten()) {
            auswertung.entfernenPosten(posten);
        }

        entityManager.remove(auswertung);
    }

}
