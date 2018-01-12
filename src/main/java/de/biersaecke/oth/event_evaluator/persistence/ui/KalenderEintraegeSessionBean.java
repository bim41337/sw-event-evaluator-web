package de.biersaecke.oth.event_evaluator.persistence.ui;

import de.biersaecke.oth.event_evaluator.persistence.entities.Eintrag;
import de.biersaecke.oth.event_evaluator.persistence.entities.Kalender;
import de.biersaecke.oth.event_evaluator.persistence.services.KalenderService;
import de.biersaecke.oth.event_evaluator.persistence.utils.GeneralConstants;
import de.biersaecke.oth.event_evaluator.persistence.utils.ZeitraumUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.Getter;
import lombok.Setter;

/**
 * Bean zur Anzeige von Einträgen eines Kalenders
 *
 * @author bim41337
 */
@Named( value = "kalenderEintraegeBean" )
@SessionScoped
public class KalenderEintraegeSessionBean implements Serializable {

    @Inject
    private KalenderService kalenderService;

    @Getter
    private List<Eintrag> kalenderEintraege;
    @Getter
    @Setter
    private Kalender kalender;

    @Getter
    @Setter
    private Date filterVon;
    @Getter
    @Setter
    private Date filterBis;

    /** Creates a new instance of kalenderEintraegeSessionBean */
    public KalenderEintraegeSessionBean() {
    }

    public String anzeigenEintraege(Kalender pKalender) {
        kalender = pKalender;
        kalenderEintraege = kalenderService.holenEintraegeZuKalender(kalender
                .getId());
        Collections.sort(kalenderEintraege);

        return GeneralConstants.NAV_CASE_EINTRAEGE_UEBERSICHT;
    }

    public String anzeigenEintraegeGefiltert() {
        kalenderEintraege = kalenderService
                .holenEintraegeZuKalenderInZeitraum(kalender.getId(), ZeitraumUtils
                        .erstellenStandardZeitraum(filterVon, filterBis));
        Collections.sort(kalenderEintraege);

        return GeneralConstants.NAV_CASE_CURRENT_PAGE;
    }

    @PostConstruct
    private void clear() {
        filterVon = new Date();
        filterBis = new Date();
        kalenderEintraege = new ArrayList<>();
    }

}
