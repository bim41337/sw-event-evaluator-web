package de.biersaecke.oth.event_evaluator.persistence.ui;

import de.biersaecke.oth.event_evaluator.persistence.entities.Bewertung;
import de.biersaecke.oth.event_evaluator.persistence.entities.Eintrag;
import de.biersaecke.oth.event_evaluator.persistence.entities.Kalender;
import de.biersaecke.oth.event_evaluator.persistence.entities.Schlagwort;
import de.biersaecke.oth.event_evaluator.persistence.services.KalenderService;
import de.biersaecke.oth.event_evaluator.persistence.utils.GeneralConstants;
import de.biersaecke.oth.event_evaluator.persistence.utils.GeneralUtils;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

/**
 * Bean zum Anlegen und Bearbeiten von Einträgen
 *
 * @author bim41337
 */
@Named( value = "editEintragBean" )
@SessionScoped
public class EditEintragSessionBean implements Serializable {

    @Inject
    private KalenderService kalenderService;
    @Inject
    private KalenderEintraegeSessionBean kalenderEintraegeBean;
    @Inject
    private UserSessionBean userBean;

    @Getter
    @Setter
    private Eintrag eintrag;

    @Getter
    private boolean eigenerEintrag;
    @Getter
    @Setter
    private Date zeitraumZeitStart;
    @Getter
    @Setter
    private Date zeitraumZeitEnde;
    @Getter
    @Setter
    private boolean bewertet;
    @Getter
    @Setter
    private Bewertung bewertung;
    @Getter
    @Setter
    private String schlagworte;

    /** Creates a new instance of EditEintragSessionBean */
    public EditEintragSessionBean() {
    }

    public String addEintragForm(Kalender kalender) {
        eintrag = new Eintrag();
        eintrag.initialisierenZeitraum();
        eintrag.setKalender(kalender);
        initialisierenProperties();

        return GeneralConstants.NAV_CASE_ADD_EINTRAG;
    }

    public String editEintragForm(Eintrag pEintrag) {
        eintrag = pEintrag;
        initialisierenProperties();

        return GeneralConstants.NAV_CASE_EDIT_EINTRAG;
    }

    public String viewEintrag(Eintrag pEintrag) {
        eintrag = pEintrag;
        initialisierenProperties();

        return GeneralConstants.NAV_CASE_VIEW_EINTRAG;
    }

    public String speichernEintrag() {
        if (bewertet) {
            eintrag.setBewertung(bewertung);
            if (bewertung.getWert() == null) {
                bewertung.setWert(0.);
            }
        }
        try {
            GeneralUtils.pruefeKalendereintrag(eintrag);
        } catch (RuntimeException ex) {
            FacesContext.getCurrentInstance().
                    addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex
                            .getMessage(), null));
            return GeneralConstants.NAV_CASE_ERROR;
        }
        mappenZeitraumZeiten();
        mappenSchlagworte();

        eintrag = (eintrag.getId() == null)
                ? kalenderService.erzeugenEintrag(eintrag)
                : kalenderService.aendernEintrag(eintrag);

        return kalenderEintraegeBean.anzeigenEintraege(eintrag.getKalender());
    }

    public String loeschenEintrag() {
        Kalender eintragKalender = eintrag.getKalender();
        try {
            kalenderService.loeschenEintrag(eintrag.getId());
            eintrag = null;
        } catch (RuntimeException ex) {
            FacesContext.getCurrentInstance().
                    addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex
                            .getMessage(), null));
            return GeneralConstants.NAV_CASE_ERROR;
        }

        return kalenderEintraegeBean.anzeigenEintraege(eintragKalender);
    }

    private void initialisierenProperties() {
        if (eintrag.getId() == null) {
            holenZeitraumZeiten();
            bewertung = new Bewertung();
            bewertet = false;
            schlagworte = GeneralUtils.EMPTY_STRING;
            eigenerEintrag = true;
        } else {
            holenZeitraumZeiten();
            holenSchlagworte();
            bewertet = eintrag.getBewertung() != null;
            if (bewertet) {
                bewertung = eintrag.getBewertung();
            } else {
                bewertung = new Bewertung();
            }
            if (!userBean.isLoggedIn()) {
                eigenerEintrag = false;
            } else {
                eigenerEintrag = userBean
                        .holenBenutzerZuEintrag(eintrag.getId())
                        .equals(userBean.getBenutzer());
            }
        }
    }

    private void mappenSchlagworte() {
        eintrag.clearSchlagworte();
        // Set enthält nur eindeutige Werte
        Arrays.stream(schlagworte.trim().split(" "))
                .filter(StringUtils::isAlpha)
                .map(s -> new Schlagwort(s))
                .collect(Collectors.toSet())
                .forEach(eintrag::hinzufuegenSchlagwort);
    }

    private void mappenZeitraumZeiten() {
        eintrag
                .setStart(setzenZeitInDatum(eintrag.getStart(), zeitraumZeitStart));
        eintrag.setEnde(setzenZeitInDatum(eintrag.getEnde(), zeitraumZeitEnde));
    }

    private Date setzenZeitInDatum(Date datum, Date zeit) {
        Calendar c = Calendar.getInstance();
        c.setTime(zeit);
        Date datumMitZeit = DateUtils.setHours(datum, c
                .get(Calendar.HOUR_OF_DAY));
        datumMitZeit = DateUtils
                .setMinutes(datumMitZeit, c.get(Calendar.MINUTE));

        return datumMitZeit;
    }

    private void holenSchlagworte() {
        Set<Schlagwort> schlagwortErgebnis = kalenderService
                .holenSchlagworteZuEintrag(eintrag.getId());
        schlagworte = StringUtils.join(schlagwortErgebnis, " ");
    }

    private void holenZeitraumZeiten() {
        zeitraumZeitStart = extrahierenZeitAusDate(eintrag.getStart());
        zeitraumZeitEnde = extrahierenZeitAusDate(eintrag.getEnde());
    }

    private Date extrahierenZeitAusDate(Date datum) {
        return DateUtils.truncate(datum, Calendar.HOUR_OF_DAY);
    }

}
