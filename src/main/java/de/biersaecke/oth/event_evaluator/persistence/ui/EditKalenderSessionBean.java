package de.biersaecke.oth.event_evaluator.persistence.ui;

import de.biersaecke.oth.event_evaluator.persistence.entities.Kalender;
import de.biersaecke.oth.event_evaluator.persistence.services.KalenderService;
import de.biersaecke.oth.event_evaluator.persistence.utils.GeneralConstants;
import de.biersaecke.oth.event_evaluator.persistence.utils.IncorrectEntityConfigurationException;
import de.biersaecke.oth.event_evaluator.persistence.utils.api.OptionKalender;
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.Logger;

/**
 * Bean zum Anlegen und Bearbeiten von Kalendern
 *
 * @author bim41337
 */
@Named( value = "editKalenderBean" )
@SessionScoped
public class EditKalenderSessionBean implements Serializable {

    @Inject
    @OptionKalender
    private Logger logger;

    @Inject
    private KalenderService kalenderService;
    @Inject
    private UserSessionBean userBean;

    @Getter
    @Setter
    private Kalender kalender;

    /** Creates a new instance of EditKalenderSessionBean */
    public EditKalenderSessionBean() {
    }

    public String addKalenderForm() {
        kalender = new Kalender();
        kalender.setBenutzer(userBean.getBenutzer());

        return GeneralConstants.NAV_CASE_ADD_KALENDER;
    }

    public String editKalenderForm(Kalender pKalender) {
        kalender = pKalender;

        return GeneralConstants.NAV_CASE_EDIT_KALENDER;
    }

    public String speichernKalender() {
        try {
            if (kalender.getId() == null) {
                kalender = kalenderService.erzeugenKalender(kalender);
            } else {
                kalender = kalenderService.aendernKalender(kalender);
            }
        } catch (IncorrectEntityConfigurationException ex) {
            FacesContext.getCurrentInstance().
                    addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex
                            .getMessage(), null));

            return GeneralConstants.NAV_CASE_ERROR;
        }

        return GeneralConstants.NAV_CASE_KALENDER_UEBERSICHT;
    }

    public String loeschenKalender() {
        try {
            kalenderService.loeschenKalender(kalender.getId());
            kalender = null;
        } catch (RuntimeException ex) {
            FacesContext.getCurrentInstance().
                    addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, ex
                            .getMessage(), null));

            return GeneralConstants.NAV_CASE_ERROR;
        }

        return GeneralConstants.NAV_CASE_KALENDER_UEBERSICHT;
    }

}
