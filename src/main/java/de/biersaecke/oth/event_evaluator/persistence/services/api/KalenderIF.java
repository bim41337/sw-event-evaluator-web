package de.biersaecke.oth.event_evaluator.persistence.services.api;

import de.biersaecke.oth.event_evaluator.persistence.entities.Eintrag;
import java.util.Date;
import java.util.List;

/**
 * Öffentliche Schnittstelle zum KalenderService
 *
 * @author bim41337
 */
public interface KalenderIF {

    /**
     * Liefert eine Liste der am übergebenen Tag veröffentlichten Einträge
     *
     * @param datum betreffender Tag
     * @param ort Veranstaltungsort
     * @return Liste der öffentlichen Einträge
     */
    List<Eintrag> holenOeffentlicheEintraegeFuerTag(Date datum, String ort);

}
