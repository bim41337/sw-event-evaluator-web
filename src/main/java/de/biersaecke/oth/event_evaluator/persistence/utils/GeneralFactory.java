package de.biersaecke.oth.event_evaluator.persistence.utils;

import de.biersaecke.oth.event_evaluator.persistence.services.AuswertungService;
import de.biersaecke.oth.event_evaluator.persistence.services.BenutzerService;
import de.biersaecke.oth.event_evaluator.persistence.services.KalenderService;
import de.biersaecke.oth.event_evaluator.persistence.utils.api.OptionAuswertung;
import de.biersaecke.oth.event_evaluator.persistence.utils.api.OptionBenutzer;
import de.biersaecke.oth.event_evaluator.persistence.utils.api.OptionKalender;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Sammelklasse für Factory-Methoden
 *
 * @author bim41337
 */
@ApplicationScoped
public class GeneralFactory {

    @Produces
    @ApplicationScoped
    @OptionAuswertung
    public Logger getAuswertungLogger() {
        return LogManager.getLogger(AuswertungService.class);
    }

    @Produces
    @ApplicationScoped
    @OptionBenutzer
    public Logger getBenutzerLogger() {
        return LogManager.getLogger(BenutzerService.class);
    }

    @Produces
    @ApplicationScoped
    @OptionKalender
    public Logger getKalenderLogger() {
        return LogManager.getLogger(KalenderService.class);
    }

}
