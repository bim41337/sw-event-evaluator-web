package de.biersaecke.oth.event_evaluator.persistence.ui;

import de.biersaecke.oth.event_evaluator.persistence.entities.Eintrag;
import de.biersaecke.oth.event_evaluator.persistence.services.KalenderService;
import de.biersaecke.oth.event_evaluator.persistence.utils.GeneralConstants;
import de.biersaecke.oth.event_evaluator.persistence.utils.Zeitraum;
import de.biersaecke.oth.event_evaluator.persistence.utils.ZeitraumUtils;
import de.biersaecke.oth.event_evaluator.persistence.utils.api.OptionKalender;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.Logger;

/**
 * Bean für die Suche von Einträgen
 *
 * @author bim41337
 */
@Named( value = "sucheBean" )
@SessionScoped
public class SucheSessionBean implements Serializable {

    @Inject
    @OptionKalender
    private Logger logger;

    @Inject
    private KalenderService kalenderService;
    @Inject
    private UserSessionBean userBean;

    @Getter
    @Setter
    private String suchBegriff;
    @Getter
    @Setter
    private Date zeitraumStart;
    @Getter
    @Setter
    private Date zeitraumEnde;
    @Getter
    @Setter
    private List<Eintrag> suchErgebnisEintraege;

    /** Creates a new instance of PublicSucheSessionBean */
    public SucheSessionBean() {
    }

    public String suchenEintraege() {
        Zeitraum suchZeitraum = ZeitraumUtils
                .erstellenStandardZeitraum(zeitraumStart, zeitraumEnde);

        if (!ZeitraumUtils.isGueltigerZeitraum(suchZeitraum)) {
            FacesContext.getCurrentInstance().
                    addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Angegebener Zeitraum ist ungültig", null));
            return GeneralConstants.NAV_CASE_ERROR;
        }

        if (userBean.isLoggedIn()) {
            suchErgebnisEintraege = kalenderService
                    .suchenEintraegeFuerUser(suchBegriff, suchZeitraum, userBean
                            .getBenutzer());
        } else {
            suchErgebnisEintraege = kalenderService
                    .suchenOeffentlicheEintraege(suchBegriff, suchZeitraum);
        }

        logger.info("Einträge gefunden: " + suchErgebnisEintraege.size());
        return GeneralConstants.NAV_CASE_SUCHE;
    }

    public String neueSuche() {
        suchBegriff = "";
        suchErgebnisEintraege = Collections.emptyList();
        return GeneralConstants.NAV_CASE_SUCHE;
    }

    public String getSuchzeitraumFormated() {
        return ZeitraumUtils
                .formatierenZeitraum(zeitraumStart, zeitraumEnde, false);
    }

    @PostConstruct
    private void clearContent() {
        suchBegriff = "";
        suchErgebnisEintraege = Collections.emptyList();
        zeitraumStart = zeitraumEnde = new Date();
    }

}
