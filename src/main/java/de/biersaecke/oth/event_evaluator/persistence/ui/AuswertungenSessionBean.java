package de.biersaecke.oth.event_evaluator.persistence.ui;

import de.biersaecke.oth.event_evaluator.persistence.entities.Auswertung;
import de.biersaecke.oth.event_evaluator.persistence.entities.Eintrag;
import de.biersaecke.oth.event_evaluator.persistence.services.AuswertungService;
import de.biersaecke.oth.event_evaluator.persistence.ui.utils.AuswertungenConverter;
import de.biersaecke.oth.event_evaluator.persistence.utils.GeneralConstants;
import de.biersaecke.oth.event_evaluator.persistence.utils.Zeitraum;
import de.biersaecke.oth.event_evaluator.persistence.utils.ZeitraumUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.Getter;
import lombok.Setter;

/**
 * Bean zur Verwaltung des Pageflows für Auswertungen
 *
 * @author bim41337
 */
@Named( value = "auswertungenBean" )
@SessionScoped
public class AuswertungenSessionBean implements Serializable {

    @Inject
    private AuswertungService auswertungService;
    @Inject
    private UserSessionBean userBean;

    @Getter
    private AuswertungenConverter auswertungenConverter;

    @Getter
    @Setter
    private List<Auswertung> auswertungenList;
    @Getter
    @Setter
    private Auswertung gewaehlteAuswertung;
    @Getter
    @Setter
    private Auswertung dropdownWahlAuswertung;

    @Getter
    @Setter
    private Date zeitraumStart;
    @Getter
    @Setter
    private Date zeitraumEnde;

    /** Creates a new instance of AuswertungenSessionBean */
    public AuswertungenSessionBean() {
    }

    public String startenAuswertungen() {
        gewaehlteAuswertung = null;
        dropdownWahlAuswertung = null;
        erneuernAuswertungenListe();
        return GeneralConstants.NAV_CASE_AUSWERTUNGEN;
    }

    public String zeitraumAuswerten() {
        Zeitraum auswertungszeitraum = ZeitraumUtils
                .erstellenStandardZeitraum(zeitraumStart, zeitraumEnde);

        if (!ZeitraumUtils.isGueltigerZeitraum(auswertungszeitraum)) {
            FacesContext.getCurrentInstance().
                    addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Angegebener Zeitraum ist ungültig", null));
            return GeneralConstants.NAV_CASE_ERROR;
        }

        if (ZeitraumUtils.isZukunft(auswertungszeitraum)) {
            FacesContext.getCurrentInstance().
                    addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Der Auswertungszeitraum muss zumindest teilweise in der Vergangenheit liegen", null));
            return GeneralConstants.NAV_CASE_ERROR;
        }

        Auswertung neueAuswertung = new Auswertung(auswertungszeitraum);
        neueAuswertung.setBenutzer(userBean.getBenutzer());
        fuellenGewaehlteAuswertung(neueAuswertung);

        return GeneralConstants.NAV_CASE_AUSWERTUNGEN;
    }

    public String anzeigenAuswertung() {
        if (dropdownWahlAuswertung != null) {
            fuellenGewaehlteAuswertung(dropdownWahlAuswertung);

            return GeneralConstants.NAV_CASE_AUSWERTUNGEN;
        }

        return GeneralConstants.NAV_CASE_CURRENT_PAGE;
    }

    public String speichernAuswertung() {
        if (gewaehlteAuswertung != null && gewaehlteAuswertung.getId() == null) {

            if (gewaehlteAuswertung.getPosten().isEmpty()) {
                FacesContext.getCurrentInstance().
                        addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Auswertung muss mindestens einen gültig bewerteten Eintrag enthalten", null));
                return GeneralConstants.NAV_CASE_ERROR;
            }

            gewaehlteAuswertung.setBenutzer(userBean.getBenutzer());
            auswertungService.speichernAuswertung(gewaehlteAuswertung);
            erneuernAuswertungenListe();
        }

        return GeneralConstants.NAV_CASE_AUSWERTUNGEN;
    }

    public String loeschenAuswertung() {
        if (gewaehlteAuswertung != null && gewaehlteAuswertung.getId() != null) {
            auswertungService.loeschenAuswertung(gewaehlteAuswertung);
        }

        return startenAuswertungen();
    }

    public String getPostenChartDatenString() {
        StringBuilder chartDataBuilder = new StringBuilder();
        if (gewaehlteAuswertung != null) {
            double postenBewertung;
            Iterator<Eintrag> iterator = gewaehlteAuswertung.getPosten()
                    .iterator();
            Eintrag posten;

            while (iterator.hasNext()) {
                posten = iterator.next();
                postenBewertung = posten.getBewertung() != null ? posten
                        .getBewertung().getWert() : 0.0f;
                chartDataBuilder.append(String.format(
                        Locale.ROOT,
                        "{ y: %.1f, label: '%.1f', indexLabel: '%s' }%s",
                        postenBewertung,
                        postenBewertung,
                        posten.getTitel(),
                        iterator.hasNext() ? "," : ""
                ));
            }
        }

        return chartDataBuilder.toString();
    }

    private void fuellenGewaehlteAuswertung(Auswertung auswertung) {
        gewaehlteAuswertung = auswertungService
                .fuellenAuswertung(auswertung);
    }

    private void erneuernAuswertungenListe() {
        auswertungenList.clear();
        auswertungenList.addAll(auswertungService
                .holenAuswertungenFuerBenutzer(userBean.getBenutzer().getId()));
    }

    @PostConstruct
    private void initialize() {
        zeitraumStart = new Date();
        zeitraumEnde = new Date();
        auswertungenConverter = new AuswertungenConverter();
        auswertungenConverter.setAuswertungService(auswertungService);
        auswertungenList = new ArrayList<>();
    }

}
