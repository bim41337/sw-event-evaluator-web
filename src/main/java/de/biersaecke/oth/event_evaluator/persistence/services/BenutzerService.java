package de.biersaecke.oth.event_evaluator.persistence.services;

import de.biersaecke.oth.event_evaluator.persistence.entities.Benutzer;
import de.biersaecke.oth.event_evaluator.persistence.entities.Eintrag;
import de.biersaecke.oth.event_evaluator.persistence.entities.Kalender;
import de.biersaecke.oth.event_evaluator.persistence.utils.GeneralUtils;
import de.biersaecke.oth.event_evaluator.persistence.utils.api.OptionBenutzer;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import org.apache.logging.log4j.Logger;

/**
 * Service für Benutzerobjekte
 *
 * @author bim41337
 */
@RequestScoped
public class BenutzerService {

    @PersistenceContext( unitName = "eventevalPU" )
    private EntityManager entityManager;

    @Inject
    private KalenderService kalenderService;

    @Inject
    @OptionBenutzer
    private Logger logger;

    @Transactional
    public Benutzer registrierenBenutzer(Benutzer benutzer) {

        GeneralUtils.pruefeRegistrierung(benutzer, entityManager);

        String pwHash = GeneralUtils.getSHA512Hash(benutzer.getPasswort());
        benutzer.setPasswort(pwHash);
        entityManager.persist(benutzer);

        // Initialen Kalender für Benutzer anlegen
        Kalender initKal = new Kalender(Kalender.DEFAULT_NAME);
        initKal.setBenutzer(benutzer);
        kalenderService.erzeugenKalender(initKal);

        logger.info("Neuer Benutzer: " + benutzer);
        return benutzer;
    }

    @Transactional
    public Benutzer loginBenutzer(Benutzer benutzer) {
        TypedQuery<Benutzer> benutzerQuery = entityManager.
                createNamedQuery(Benutzer.NQ_PARAMS_LOGIN, Benutzer.class);

        benutzerQuery.setParameter("bName", benutzer.getName());
        benutzerQuery.setParameter("bPw", GeneralUtils.getSHA512Hash(benutzer.
                getPasswort()));
        try {
            Benutzer benutzerResult = benutzerQuery.getSingleResult();
            return benutzerResult;
        } catch (NoResultException | NonUniqueResultException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Kein Nutzer mit diesen Daten vorhanden.");
        }
    }

    @Transactional
    public Benutzer loeschenBenutzer(Benutzer benutzer) {
        benutzer = entityManager.merge(benutzer);
        entityManager.remove(benutzer);

        return benutzer;
    }

    @Transactional
    public Benutzer holenBenutzerZuEintrag(Long eintragId) {
        Eintrag eintrag = entityManager.find(Eintrag.class, eintragId);

        if (eintrag == null) {
            throw new RuntimeException("Ungültige Eintrag-ID - Benutzer zu Eintrag nicht gefunden");
        }

        return eintrag.getKalender().getBenutzer();
    }

}
