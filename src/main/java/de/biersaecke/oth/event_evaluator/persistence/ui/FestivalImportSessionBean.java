package de.biersaecke.oth.event_evaluator.persistence.ui;

import de.biersaecke.oth.event_evaluator.persistence.entities.Festival;
import de.biersaecke.oth.event_evaluator.persistence.entities.Kalender;
import de.biersaecke.oth.event_evaluator.persistence.services.KalenderService;
import de.biersaecke.oth.event_evaluator.persistence.utils.GeneralConstants;
import de.biersaecke.oth.event_evaluator.persistence.utils.GeneralUtils;
import java.io.Serializable;
import java.util.List;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.Getter;
import lombok.Setter;

/**
 * Bean zum Import von Festivals aus dem Partnerprojekt
 *
 * @author bim41337
 */
@Named( value = "festivalImportBean" )
@SessionScoped
public class FestivalImportSessionBean implements Serializable {

    @Inject
    private KalenderService kalenderService;
    @Inject
    private KalenderEintraegeSessionBean kalenderEintraegeBean;

    @Getter
    @Setter
    private Kalender kalender;
    @Getter
    @Setter
    private List<Festival> festivals;

    /** Creates a new instance of FestivalImportSessionBean */
    public FestivalImportSessionBean() {
    }

    public String startenFestivalImport(Kalender pKalender) {
        kalender = pKalender;
        festivals = kalenderService.holenFreigegebeneFestivals();

        return GeneralConstants.NAV_CASE_FESTIVALS;
    }

    public String importierenFestival(Festival festival) {
        festival.setKalender(kalender);
        try {
            GeneralUtils.pruefeKalendereintrag(festival);
        } catch (RuntimeException ex) {
            FacesContext.getCurrentInstance().
                    addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex
                            .getMessage(), null));
            return GeneralConstants.NAV_CASE_ERROR;
        }
        kalenderService.erzeugenEintrag(festival);

        return kalenderEintraegeBean.anzeigenEintraege(kalender);
    }

}
