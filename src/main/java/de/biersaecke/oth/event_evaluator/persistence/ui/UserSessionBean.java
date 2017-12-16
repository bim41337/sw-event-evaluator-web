package de.biersaecke.oth.event_evaluator.persistence.ui;

import de.biersaecke.oth.event_evaluator.persistence.entities.Benutzer;
import de.biersaecke.oth.event_evaluator.persistence.entities.Kalender;
import de.biersaecke.oth.event_evaluator.persistence.services.BenutzerService;
import de.biersaecke.oth.event_evaluator.persistence.services.KalenderService;
import de.biersaecke.oth.event_evaluator.persistence.utils.GeneralConstants;
import de.biersaecke.oth.event_evaluator.persistence.utils.IncorrectEntityConfigurationException;
import de.biersaecke.oth.event_evaluator.persistence.utils.api.OptionBenutzer;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.Logger;

/**
 * Bean zur Verwaltung der Nutzer-Session
 *
 * @author bim41337
 */
@Named( value = "userBean" )
@SessionScoped
public class UserSessionBean implements Serializable {

    @Inject
    private BenutzerService benutzerService;
    @Inject
    private KalenderService kalenderService;
    @Inject
    @OptionBenutzer
    private Logger serviceLogger;

    @Getter
    @Setter
    private Benutzer benutzer;
    @Getter
    @Setter
    private boolean loggedIn = false;

    @Getter
    @Setter
    private String loginName;
    @Getter
    @Setter
    private String loginPasswort;

    /** Creates a new instance of UserSessionBean */
    public UserSessionBean() {
    }

    public String loginUser() {
        try {
            benutzer = benutzerService.
                    loginBenutzer(new Benutzer(loginName, loginPasswort));
            loggedIn = true;
        } catch (RuntimeException ex) {
            serviceLogger.error("loginUser() - Fehler: " + ex.getClass().
                    getSimpleName());
        }

        if (!loggedIn) {
            FacesContext.getCurrentInstance().
                    addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Benutzer nicht gefunden", null));
            return GeneralConstants.NAV_CASE_ERROR;
        }

        return GeneralConstants.NAV_CASE_KALENDER_UEBERSICHT;
    }

    public String logoutBenutzer() {
        benutzer = null;
        loggedIn = false;

        return GeneralConstants.NAV_CASE_INDEX;
    }

    public String registrierenUser() {
        try {
            benutzer = benutzerService.
                    registrierenBenutzer(new Benutzer(loginName, loginPasswort));
            loggedIn = true;
        } catch (IncorrectEntityConfigurationException ex) {
            FacesContext.getCurrentInstance().
                    addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex
                            .getMessage(), null));

            return GeneralConstants.NAV_CASE_ERROR;
        }

        return GeneralConstants.NAV_CASE_KALENDER_UEBERSICHT;
    }

    public List<Kalender> holenBenutzerKalender() throws IOException {
        if (loggedIn) {
            List<Kalender> benutzerKalender = kalenderService.
                    holenKalenderFuerBenutzer(benutzer);
            Collections.sort(benutzerKalender, (e1, e2) -> e1.getBezeichnung().
                    compareTo(e2.getBezeichnung()));

            return benutzerKalender;
        } else {
            ExternalContext externalContext = FacesContext.getCurrentInstance().
                    getExternalContext();
            externalContext.
                    redirect(externalContext.getRequestContextPath() + "/" + GeneralConstants.PAGE_NAME_LOGIN);
            return Collections.emptyList();
        }
    }

    public Benutzer holenBenutzerZuEintrag(Long eintragId) {
        return benutzerService.holenBenutzerZuEintrag(eintragId);
    }

}
